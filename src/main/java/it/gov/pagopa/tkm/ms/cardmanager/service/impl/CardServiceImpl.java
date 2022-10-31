package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCitizen;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCitizenCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentResponse;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueueToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueueCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueueToken;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenCardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.CardService;
import it.gov.pagopa.tkm.ms.cardmanager.service.CircuitBreakerManager;
import it.gov.pagopa.tkm.util.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.*;
import static it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.CardActionEnum.INSERT_UPDATE;

@Service
@Log4j2
public class CardServiceImpl implements CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CitizenCardRepository citizenCardRepository;

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private RtdHashingClient rtdHashingClient;

    @Autowired
    private ConsentClient consentClient;

    @Autowired
    private ProducerServiceImpl producerService;

    @Autowired
    private CryptoServiceImpl cryptoService;

    @Value("${keyvault.apimSubscriptionTkmRtd}")
    private String apimRtdSubscriptionKey;

    @Autowired
    private CardTokenRepository cardTokenRepository;

    @Autowired
    private CircuitBreakerManager circuitBreakerManager;

    @Override
    public void updateOrCreateCard(ReadQueue readQueue) {
        if (StringUtils.isBlank(readQueue.getTaxCode())) {
            manageNonIssuerCases(readQueue);
        } else {
            manageIssuerCases(readQueue);
        }
    }

    //NON-ISSUER

    private void manageNonIssuerCases(ReadQueue readQueue) {
        String par = readQueue.getPar();
        String hpan = readQueue.getHpan();
        CircuitEnum circuit = readQueue.getCircuit();
        List<ReadQueueToken> tokens = readQueue.getTokens();
        if (StringUtils.isNotBlank(par) && StringUtils.isBlank(hpan) && CollectionUtils.isNotEmpty(tokens)) {
            checkCollectionSingleton(tokens);
            manageParAndToken(par, circuit, tokens);
        } else if (StringUtils.isNoneBlank(par, hpan) && CollectionUtils.isEmpty(tokens)) {
            manageParAndHpan(par, hpan, circuit);
        } else if (StringUtils.isAllBlank(par, hpan) && CollectionUtils.isNotEmpty(tokens)) {
            manageOnlyToken(tokens, circuit);
        } else {
            throw new CardException(INCONSISTENT_MESSAGE);
        }
    }

    private void checkCollectionSingleton(List<ReadQueueToken> tokens) {
        if (CollectionUtils.size(tokens) > 1) {
            throw new CardException(INCONSISTENT_MESSAGE);
        }
    }

    private void manageParAndHpan(String par, String hpan, CircuitEnum circuit) {
        log.info("manageParAndHpan with par " + ObfuscationUtils.obfuscatePar(par) + " and hpan " + ObfuscationUtils.obfuscateHpan(hpan));
        TkmCard cardByHpanAndPar = cardRepository.findByHpanAndPar(hpan, par);
        if (cardByHpanAndPar != null) {
            cardByHpanAndPar.setCircuit(circuit);
            cardRepository.save(cardByHpanAndPar);
            log.info("Card found by par " + ObfuscationUtils.obfuscatePar(par) + " and hpan " + ObfuscationUtils.obfuscateHpan(hpan) + " aborting");
            return;
        }
        TkmCard cardByHpan = cardRepository.findByHpan(hpan);
        TkmCard cardByPar = cardRepository.findByPar(par);
        if (cardByHpan != null) {
            log.info("Found card by hpan " + ObfuscationUtils.obfuscateHpan(hpan) + ", updating " + cardByHpan.getId() + " - old PAR is " + ObfuscationUtils.obfuscatePar(cardByHpan.getPar()));
            log.trace(cardByHpan);

            // if the card in the DB has a different not null PAR throw exception
            if (StringUtils.isNotEmpty(cardByHpan.getPar()) && !StringUtils.equals(cardByHpan.getPar(), par)) {
                throw new CardException(INCONSISTENT_MESSAGE);
            }

            if (cardByPar != null) {
                // there is already a card in DB with the same PAR of input one but different hpan
                // the card in DB should be a "fake" card used to temporary store tokens

                log.info("Also found card by par " + ObfuscationUtils.obfuscatePar(par) + " with id " + cardByPar.getId() + " and hpan " + ObfuscationUtils.obfuscateHpan(cardByPar.getHpan()) + ", merging");
                log.trace(cardByPar);

                // if the card in the DB has a different not null hpan throw exception
                if (StringUtils.isNotEmpty(cardByPar.getHpan()) && !StringUtils.equals(cardByPar.getHpan(), cardByHpan.getHpan())) {
                    throw new CardException(DUPLICATE_PAR);
                }

                // the card in DB is a "fake" card so we re-link citizen to the real card with pan
                // it is not clear how can exists a citizen linked to a "fake" card
                updateCitizenCardAfterMerge(cardByHpan, cardByPar);

                // the card in DB is a "fake" card so we re-link token to the real card with pan
                mergeTokenIntoParCardToken(cardByHpan, cardByPar);
            }

            deleteIfNotNull(cardByPar);

            cardByHpan.setPar(par);
            cardByHpan.setLastUpdateDate(Instant.now());
            cardByHpan.setCircuit(circuit);
            cardRepository.save(cardByHpan);

            // it was done before only on citizen linked to cardByPar (if not null)
            // but there could be citizen linked also to cardByPan before, and now we receive PAR
            // so we should send update to CSTAR
            writeOnQueueIfCompleteForUpdatedCard(cardByHpan, cardByHpan.getTokens());

        } else if (cardByPar != null) {
            log.info("Found card by par " + ObfuscationUtils.obfuscatePar(par) + ", updating");

            // in this case we receive a couple hpan/par not null
            // there is already a card in DB with the same PAR of input
            // there is no card in DB with the same hpan
            // it is not clear the use case here (par retriever only knows hpan from card manager)

            // in any case, if the card in the DB has a different not null hpan throw exception
            if (StringUtils.isNotEmpty(cardByPar.getHpan()) && !StringUtils.equals(cardByPar.getHpan(), cardByHpan.getHpan())) {
                throw new CardException(DUPLICATE_PAR);
            }

            cardByPar.setHpan(hpan);
            cardByPar.setLastUpdateDate(Instant.now());
            cardByPar.setCircuit(circuit);
            cardRepository.save(cardByPar);
        } else {
            log.info("No existing cards found, creating one for par " + ObfuscationUtils.obfuscatePar(par) + " and hpan " + ObfuscationUtils.obfuscateHpan(hpan));
            TkmCard card = TkmCard.builder().hpan(hpan).par(par).circuit(circuit).creationDate(Instant.now()).build();
            cardRepository.save(card);
        }
    }

    private void updateCitizenCardAfterMerge(TkmCard survivingCard, TkmCard deletedCard) {
        List<TkmCitizenCard> citizenCards = citizenCardRepository.findByCardId(deletedCard.getId());
        log.trace(citizenCards);
        citizenCards.forEach(c -> c.setCard(survivingCard));
        citizenCardRepository.saveAll(citizenCards);
        log.debug("All cards have been merged");
    }

    private void manageOnlyToken(List<ReadQueueToken> tokens, CircuitEnum circuit) {
        ReadQueueToken readQueueToken = tokens.get(0);
        String token = readQueueToken.getToken();
        String htoken = getHash(token, readQueueToken.getHToken());
        TkmCardToken byHtokenAndDeletedFalse = cardTokenRepository.findByHtokenAndDeletedFalse(htoken);
        if (byHtokenAndDeletedFalse == null) {
            log.debug("Adding htoken:" + ObfuscationUtils.obfuscateHtoken(htoken));
            TkmCard fakeCard = TkmCard.builder().circuit(circuit).creationDate(Instant.now()).build();
            TkmCardToken build = TkmCardToken.builder().htoken(htoken).token(cryptoService.encrypt(token)).card(fakeCard).creationDate(Instant.now()).build();
            fakeCard.setTokens(new HashSet<>(Collections.singleton(build)));
            cardRepository.save(fakeCard);
        } else {
            log.debug("Skip: Card Already Updated");
        }
    }

    private void manageParAndToken(String par, CircuitEnum circuit, List<ReadQueueToken> tokens) {
        ReadQueueToken readQueueToken = tokens.get(0);
        String token = readQueueToken.getToken();
        log.info("manageParAndToken with par " + ObfuscationUtils.obfuscatePar(par));
        String htoken = getHash(token, readQueueToken.getHToken());
        TkmCardToken byHtoken = cardTokenRepository.findByHtokenAndDeletedFalse(htoken);
        if (byHtoken == null) {
            log.info("Token not found by htoken " + ObfuscationUtils.obfuscateHtoken(htoken));
            byHtoken = TkmCardToken.builder().htoken(htoken).token(cryptoService.encrypt(token)).creationDate(Instant.now()).build();
        } else {
            log.info("Token found by htoken " + ObfuscationUtils.obfuscateHtoken(htoken) + " - id: " + byHtoken.getId());
            byHtoken.setLastUpdateDate(Instant.now());
        }
        //Looking for the row with the par or with the token. If they exist I'll merge them
        TkmCard cardToSave = TkmCard.builder().par(par).circuit(circuit).creationDate(Instant.now()).build();
        TkmCard tokenCard = byHtoken.getCard();

        log.info("TokenCard by htoken " + ObfuscationUtils.obfuscateHtoken(htoken) + " - id: " + (tokenCard != null ? tokenCard.getId() : "not found"));

        if (tokenCard != null && StringUtils.isNotBlank(tokenCard.getPar())) {
            log.info("Skip: Card Already Updated " + ObfuscationUtils.obfuscatePar(tokenCard.getPar()));
            return;
        }
        TkmCard parCard = cardRepository.findByPar(par);
        log.info("ParCard by par " + ObfuscationUtils.obfuscatePar(par) + " - id: " + (parCard != null ? parCard.getId() : "not found - htoken " + ObfuscationUtils.obfuscateHtoken(htoken)));

        //I prefer the row with the par and delete the one without
        if (parCard != null) {
            cardToSave = parCard;
            cardToSave.setLastUpdateDate(Instant.now());
            mergeTokenCardIntoParCard(cardToSave, tokenCard);
            deleteIfNotNull(tokenCard);
        } else if (tokenCard != null) {
            cardToSave = tokenCard;
            cardToSave.setPar(par);
        }
        byHtoken.setCard(cardToSave);
        cardToSave.getTokens().add(byHtoken);
        cardRepository.save(cardToSave);

        // we need to inform CSTAR that a new token was added to the card
        writeOnQueueIfCompleteForUpdatedCard(cardToSave, cardToSave.getTokens());
    }

    private void writeOnQueueIfCompleteForUpdatedCard(TkmCard cardToSave, Set<TkmCardToken> tokensToUpdate) {
        List<TkmCitizenCard> citizenCards = citizenCardRepository.findByCardId(cardToSave.getId());

        log.info("Write on queue for update card hpan " + ObfuscationUtils.obfuscateHpan(cardToSave.getHpan()) + " par " + ObfuscationUtils.obfuscatePar(cardToSave.getPar()) + " - " + citizenCards.size() + " citizen cards to update");

        citizenCards.forEach(c -> writeOnQueueIfComplete(c, tokensToUpdate, true));
    }

    private void mergeTokenCardIntoParCard(TkmCard cardToSave, TkmCard tokenCard) {
        //Adding pan and hpan if present to kept card
        if (tokenCard != null) {
            cardToSave.setPan(StringUtils.firstNonBlank(cardToSave.getPan(), tokenCard.getPan()));
            cardToSave.setHpan(StringUtils.firstNonBlank(cardToSave.getHpan(), tokenCard.getHpan()));
            mergeTokenIntoParCardToken(cardToSave, tokenCard);
        }
    }

    private void mergeTokenIntoParCardToken(TkmCard cardToSave, TkmCard tokenCard) {
        //moving the tokens from the card that will be deleted to the card with par
        List<TkmCardToken> tokensCard = new ArrayList<>(tokenCard.getTokens());
        Instant now = Instant.now();
        if (CollectionUtils.isNotEmpty(tokensCard)) {
            for (TkmCardToken t : tokensCard) {
                t.setLastUpdateDate(now);
                t.setCard(cardToSave);
            }
            cardToSave.getTokens().addAll(tokensCard);
        }
    }

    private void deleteIfNotNull(TkmCard tkmCard) {
        if (tkmCard != null) {
            log.info("Deleting tkmCard " + tkmCard.getId());
            cardRepository.delete(tkmCard);
            // force flush to avoid insert error for duplicate PAR from DB
            // when doing other query
            cardRepository.flush();
        }
    }

    private String getHash(String value, String hvalue) {
        if (StringUtils.isNotBlank(hvalue))
            return hvalue;
        return circuitBreakerManager.callRtdForHash(rtdHashingClient, value, apimRtdSubscriptionKey);
    }

    // ISSUER
    private void manageIssuerCases(ReadQueue readQueue) {
        String par = readQueue.getPar();
        String pan = readQueue.getPan();
        String hpan = (readQueue.getHpan() == null && pan != null) ? getHash(pan, null) : readQueue.getHpan();
        String taxCode = readQueue.getTaxCode();
        CircuitEnum circuit = readQueue.getCircuit();
        Set<TkmCardToken> tokens = queueTokensToEncryptedTkmTokens(readQueue.getTokens());
        List<String> htokens = tokens == null ? new ArrayList<>() : tokens.stream().map(TkmCardToken::getHtoken).collect(Collectors.toList());
        TkmCard card = getOrCreateCard(hpan, par, pan, circuit);
        TkmCitizen citizen = getOrCreateCitizen(taxCode);
        Set<TkmCardToken> oldTokens = new HashSet<>(card.getTokens());
        boolean merged = mergeCards(card, pan, hpan, par, htokens, citizen);
        manageAndEncryptTokens(card, tokens);
        log.info("Merged tokens: " + card.getTokens().stream().map(c -> ObfuscationUtils.obfuscateHtoken(c.getHtoken())).collect(Collectors.joining(", ")));
        TkmCitizenCard citizenCard = getOrCreateCitizenCard(taxCode, card, citizen);
        citizenCardRepository.save(citizenCard);
        writeOnQueueIfComplete(citizenCard, oldTokens, merged);
    }

    private TkmCitizen getOrCreateCitizen(String taxCode) {
        TkmCitizen citizen = citizenRepository.findByTaxCodeAndDeletedFalse(taxCode);
        if (citizen == null) {
            citizen = TkmCitizen.builder()
                    .taxCode(taxCode)
                    .creationDate(Instant.now())
                    .build();
        }
        return citizen;
    }

    private TkmCard getOrCreateCard(String hpan, String par, String pan, CircuitEnum circuit) {
        TkmCard card = null;
        if (hpan != null) {
            log.info("Searching for card with hpan " + ObfuscationUtils.obfuscateHpan(hpan));
            card = cardRepository.findByHpan(hpan);
        }
        if (card == null && par != null) {
            log.info("Card not found by hpan, searching by par " + ObfuscationUtils.obfuscatePar(par));
            card = cardRepository.findByPar(par);
        }
        if (card == null) {
            log.info("Card not found on database, creating new one");
            card = TkmCard.builder()
                    .circuit(circuit)
                    .pan(cryptoService.encryptNullable(pan))
                    .hpan(hpan)
                    .par(par)
                    .tokens(new HashSet<>())
                    .creationDate(Instant.now())
                    .build();
        }
        card.setCircuit(circuit);
        return card;
    }

    private TkmCitizenCard getOrCreateCitizenCard(String taxCode, TkmCard card, TkmCitizen citizen) {
        TkmCitizenCard citizenCard = null;
        String hpan = card.getHpan();
        String par = card.getPar();
        if (hpan != null) {
            log.info("Searching citizen card by taxCode " + ObfuscationUtils.obfuscateTaxCode(taxCode) + " and hpan " + ObfuscationUtils.obfuscateHpan(hpan));
            citizenCard = citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(taxCode, hpan);
        }
        if (citizenCard == null && par != null) {
            log.info("Citizen card not found by hpan, searching by par " + ObfuscationUtils.obfuscatePar(par));
            citizenCard = citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Par(taxCode, par);
        }
        if (citizenCard == null) {
            log.info("Citizen card not found, creating one");
            return TkmCitizenCard.builder()
                    .citizen(citizen)
                    .card(card)
                    .creationDate(Instant.now())
                    .build();
        }
        return citizenCard;
    }

    private boolean mergeCards(TkmCard survivingCard, String pan, String hpan, String par, List<String> htokens, TkmCitizen citizen) {
        boolean mergedByTokens = CollectionUtils.isNotEmpty(htokens) && mergeCardsByToken(survivingCard, htokens, citizen);
        boolean mergedByPanOrPar = mergeCardsByPanOrPar(survivingCard, pan, hpan, par, citizen);
        return mergedByPanOrPar || mergedByTokens;
    }

    private boolean mergeCardsByToken(TkmCard survivingCard, List<String> htokens, TkmCitizen citizen) {
        boolean toMerge = false;
        List<TkmCardToken> preexistingTokens = cardTokenRepository.findByHtokenIn(htokens);
        log.debug("Preexisting tokens: " + preexistingTokens.stream().map(t -> ObfuscationUtils.obfuscateHtoken(t.getHtoken())).collect(Collectors.joining(", ")));
        if (CollectionUtils.isNotEmpty(preexistingTokens)) {
            List<TkmCard> cardsToDelete = preexistingTokens.stream().map(TkmCardToken::getCard).filter(card -> !card.equals(survivingCard)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(cardsToDelete)) {
                log.info(preexistingTokens.size() + " preexisting matching tokens found, merging cards");
                toMerge = true;
                if (survivingCard.getHpan() == null) {
                    TkmCard panCard = cardsToDelete.stream()
                            .filter(c -> c.getHpan() != null)
                            .findFirst().orElse(null);
                    if (panCard != null) {
                        survivingCard.setPan(panCard.getPan());
                        survivingCard.setHpan(panCard.getHpan());
                    }
                } else if (survivingCard.getPar() == null) {
                    cardsToDelete.stream()
                            .filter(c -> c.getPar() != null)
                            .findFirst().ifPresent(
                                    parCard -> survivingCard.setPar(parCard.getPar())
                            );
                }
                survivingCard.getTokens().addAll(preexistingTokens);
                updateCitizenCardsAfterMergeForIssuer(survivingCard, cardsToDelete, citizen);
                cardRepository.deleteAll(cardsToDelete);
            }
        }
        return toMerge;
    }

    private boolean mergeCardsByPanOrPar(TkmCard survivingCard, String pan, String hpan, String par, TkmCitizen citizen) {
        TkmCard preexistingCard = null;
        boolean toMerge = false;
        if (par != null && survivingCard.getPar() == null) {
            preexistingCard = cardRepository.findByPar(par);
            survivingCard.setPar(par);
            survivingCard.setLastUpdateDate(Instant.now());
            toMerge = true;
        } else if ((hpan != null && survivingCard.getHpan() == null) || (pan != null && survivingCard.getPan() == null)) {
            preexistingCard = cardRepository.findByHpan(hpan);
            survivingCard.setPan(cryptoService.encryptNullable(pan));
            survivingCard.setHpan(hpan);
            survivingCard.setLastUpdateDate(Instant.now());
            toMerge = true;
        }
        if (preexistingCard != null) {
            log.info("Preexisting card found with " + (par != null ? "par " + ObfuscationUtils.obfuscatePar(par) : "hpan " + ObfuscationUtils.obfuscateHpan(hpan)) + ", merging");
            mergeTokens(preexistingCard.getTokens(), survivingCard.getTokens());
            updateCitizenCardsAfterMergeForIssuer(survivingCard, Collections.singletonList(preexistingCard), citizen);
            cardRepository.delete(preexistingCard);
        }
        return toMerge;
    }

    private void updateCitizenCardsAfterMergeForIssuer(TkmCard survivingCard, List<TkmCard> deletedCards, TkmCitizen citizen) {
        cardRepository.save(survivingCard);
        List<TkmCard> allCards = new ArrayList<>();
        allCards.add(survivingCard);
        allCards.addAll(deletedCards);
        List<TkmCitizenCard> citizenCards = citizenCardRepository.findByCardIdIn(allCards.stream().map(TkmCard::getId).filter(Objects::nonNull).collect(Collectors.toList()));
        log.trace(citizenCards);
        Map<TkmCitizen, TkmCitizenCard> citizenCardsMap = new HashMap<>();
        List<TkmCitizenCard> survivingCitizenCards = new ArrayList<>();
        List<TkmCitizenCard> citizenCardsToDelete = new ArrayList<>();
        TkmCitizenCard toExclude = citizenCards.stream().filter(c -> c.getCard().equals(survivingCard)).findFirst().orElse(null);
        if (toExclude != null) {
            citizenCardsMap.put(citizen, toExclude);
            survivingCitizenCards.add(toExclude);
        }
        for (TkmCitizenCard c : citizenCards) {
            TkmCitizen cardCitizen = c.getCitizen();
            if (citizenCardsMap.get(cardCitizen) == null) {
                citizenCardsMap.put(cardCitizen, c);
                survivingCitizenCards.add(c);
            } else if (!c.equals(toExclude)) {
                citizenCardsToDelete.add(c);
            }
        }
        citizenCardRepository.deleteAll(citizenCardsToDelete);
        survivingCitizenCards.forEach(c -> c.setCard(survivingCard));
        citizenCardRepository.saveAll(survivingCitizenCards);
        Set<TkmCardToken> tokensToUpdate = new HashSet<>();
        deletedCards.forEach(c -> tokensToUpdate.addAll(c.getTokens()));
        for (TkmCardToken t : tokensToUpdate) {
            t.setCard(survivingCard);
            t.setLastUpdateDate(Instant.now());
        }
        cardTokenRepository.saveAll(tokensToUpdate);

        citizenCards.forEach(c -> writeOnQueueIfComplete(c, tokensToUpdate, true));
        log.debug("All cards have been merged by token");
    }

    private void manageAndEncryptTokens(TkmCard card, Set<TkmCardToken> newTokens) {
        if (newTokens == null) {
            return;
        }
        newTokens.forEach(t -> t.setCard(card));
        if (CollectionUtils.isEmpty(newTokens)) {
            mergeTokens(card.getTokens(), newTokens);
        } else {
            card.getTokens().addAll(newTokens);
        }
    }

    private void mergeTokens(Set<TkmCardToken> oldTokens, Set<TkmCardToken> newTokens) {
        Instant now = Instant.now();
        oldTokens.stream().filter(t -> !newTokens.contains(t)).forEach(t -> {
            t.setDeleted(true);
            t.setLastUpdateDate(now);
        });
    }

    private Set<TkmCardToken> queueTokensToEncryptedTkmTokens(List<ReadQueueToken> readQueueTokens) {
        if (readQueueTokens == null) {
            return null;
        }
        return readQueueTokens.stream().map(t -> TkmCardToken.builder()
                .token(cryptoService.encrypt(t.getToken()))
                .htoken(getHash(t.getToken(), t.getHToken()))
                .creationDate(Instant.now())
                .build()
        ).collect(Collectors.toSet());
    }

    private void writeOnQueueIfComplete(TkmCitizenCard citizenCard, Set<TkmCardToken> oldTokens, boolean merged) {
        TkmCard card = citizenCard.getCard();
        if (StringUtils.isAnyBlank(card.getPan(), card.getPar())) {
            log.info("Card missing pan or par, not writing on queue - id " + card.getId());
            return;
        }
        String taxCode = citizenCard.getCitizen().getTaxCode();
        if (!getConsentForCard(card, taxCode)) {
            return;
        }
        try {
            WriteQueueCard writeQueueCard = new WriteQueueCard(
                    card.getHpan(),
                    INSERT_UPDATE,
                    card.getPar(),
                    getTokensDiff(oldTokens, card.getTokens(), merged)
            );
            WriteQueue writeQueue = new WriteQueue(
                    taxCode,
                    Instant.now(),
                    Collections.singleton(writeQueueCard)
            );
            producerService.sendMessage(writeQueue);
        } catch (Exception e) {
            log.error("Error writing to queue for card hpan " + ObfuscationUtils.obfuscateHpan(card.getHpan()) + " par " + ObfuscationUtils.obfuscatePar(card.getPar()), e);
            throw new CardException(MESSAGE_WRITE_FAILED);
        }
    }

    private Set<WriteQueueToken> getTokensDiff(Set<TkmCardToken> oldTokens, Set<TkmCardToken> newTokens, boolean merged) {
        return merged ?
                oldTokens.stream().map(WriteQueueToken::new).collect(Collectors.toSet())
                : newTokens.stream().filter(t -> t.isDeleted() || !oldTokens.contains(t)).map(WriteQueueToken::new).collect(Collectors.toSet());
    }

    private boolean getConsentForCard(TkmCard card, String taxCode) {
        log.info("Calling Consent Manager for card with taxCode " + ObfuscationUtils.obfuscateTaxCode(taxCode) + " and hpan " + ObfuscationUtils.obfuscateHpan(card.getHpan()));
        ConsentResponse consentResponse = circuitBreakerManager.consentClientGetConsent(consentClient, taxCode, card.getHpan());
        boolean hasConsent = consentResponse.cardHasConsent(card.getHpan());
        log.info("Card has consent? " + hasConsent);
        return hasConsent;
    }

}
