package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import feign.FeignException;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.WalletsHashingEvaluationInput;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentResponse;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueueToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueueCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueueToken;
import it.gov.pagopa.tkm.ms.cardmanager.repository.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.CardService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
        log.debug("manageParAndHpan with par " + par + " and hpan " + hpan);
        TkmCard cardByHpanAndPar = cardRepository.findByHpanAndPar(hpan, par);
        if (cardByHpanAndPar != null) {
            log.debug("A complete card with this par and hpan already exists, aborting");
            return;
        }
        TkmCard cardByHpan = cardRepository.findByHpan(hpan);
        TkmCard cardByPar = cardRepository.findByPar(par);
        if (cardByHpan != null) {
            log.debug("Found card by hpan " + hpan + ", updating");
            log.trace(cardByHpan);
            cardByHpan.setPar(par);
            cardByHpan.setLastUpdateDate(Instant.now());
            if (cardByPar != null) {
                log.debug("Also found card by par " + par + ", merging it into card found by hpan");
                log.trace(cardByPar);
                for (TkmCardToken t : cardByPar.getTokens()) {
                    t.setCard(cardByHpan);
                    t.setLastUpdateDate(Instant.now());
                }
                updateCitizenCardAfterMerge(cardByHpan, cardByPar);
                cardRepository.delete(cardByPar);
            }
            cardRepository.save(cardByHpan);
        } else if (cardByPar != null) {
            log.debug("Found card by par " + par + ", updating");
            cardByPar.setHpan(hpan);
            cardByPar.setLastUpdateDate(Instant.now());
            cardRepository.save(cardByPar);
        } else {
            log.debug("No existing cards found, creating one");
            TkmCard card = TkmCard.builder().hpan(hpan).par(par).circuit(circuit).creationDate(Instant.now()).build();
            cardRepository.save(card);
        }
    }

    private void updateCitizenCardAfterMerge(TkmCard survivingCard, TkmCard deletedCard) {
        List<TkmCitizenCard> citizenCards = citizenCardRepository.findByCardId(deletedCard.getId());
        log.trace(citizenCards);
        citizenCards.forEach(c -> c.setCard(survivingCard));
        citizenCardRepository.saveAll(citizenCards);
        citizenCards.forEach(c -> writeOnQueueIfComplete(c, deletedCard.getTokens(), true));
        log.debug("All cards have been merged");
    }

    private void manageOnlyToken(List<ReadQueueToken> tokens, CircuitEnum circuit) {
        ReadQueueToken readQueueToken = tokens.get(0);
        String token = readQueueToken.getToken();
        String htoken = getHtoken(readQueueToken.getHToken(), token);
        TkmCardToken byHtokenAndDeletedFalse = cardTokenRepository.findByHtokenAndDeletedFalse(htoken);
        if (byHtokenAndDeletedFalse == null) {
            log.debug("Adding htoken:" + htoken);
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
        log.debug("manageParAndToken with par " + par);
        String htoken = getHtoken(readQueueToken.getHToken(), token);
        TkmCardToken byHtoken = cardTokenRepository.findByHtokenAndDeletedFalse(htoken);
        if (byHtoken == null) {
            byHtoken = TkmCardToken.builder().htoken(htoken).token(cryptoService.encrypt(token)).creationDate(Instant.now()).build();
        } else {
            byHtoken.setLastUpdateDate(Instant.now());
        }
        //Looking for the row with the par or with the token. If they exist I'll merge them
        TkmCard cardToSave = TkmCard.builder().par(par).circuit(circuit).creationDate(Instant.now()).build();
        TkmCard tokenCard = byHtoken.getCard();
        log.trace("TokenCard: " + tokenCard);
        if (tokenCard != null && StringUtils.isNotBlank(tokenCard.getPar())) {
            log.debug("Skip: Card Already Updated");
            return;
        }
        TkmCard parCard = cardRepository.findByPar(par);
        log.trace("ParCard: " + parCard);
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
            cardRepository.delete(tkmCard);
        }
    }

    private String callRtdForHash(String toHash) {
        log.trace("Calling RTD for hash of " + toHash);
        try {
            return rtdHashingClient.getHash(new WalletsHashingEvaluationInput(toHash), apimRtdSubscriptionKey).getHashPan();
        } catch (Exception e) {
            log.error(e);
            throw new CardException(CALL_TO_RTD_FAILED);
        }
    }

    private String getHtoken(String htoken, String token) {
        if (StringUtils.isNotBlank(htoken))
            return htoken;
        return callRtdForHash(token);
    }

    //ISSUER

    private void manageIssuerCases(ReadQueue readQueue) {
        String par = readQueue.getPar();
        String pan = readQueue.getPan();
        String hpan = (readQueue.getHpan() == null && pan != null) ? callRtdForHash(pan) : readQueue.getHpan();
        String taxCode = readQueue.getTaxCode();
        Set<TkmCardToken> oldTokens = new HashSet<>();
        TkmCitizenCard citizenCard = findCitizenCard(taxCode, hpan, par);
        TkmCard card;
        boolean merged = false;
        if (citizenCard == null) {
            log.info("Card not found on database, creating new one");
            citizenCard = createCitizenCard(taxCode, hpan, par, pan, readQueue.getCircuit());
            card = citizenCard.getCard();
        } else {
            log.info("Card found on database, updating");
            card = citizenCard.getCard();
            oldTokens.addAll(card.getTokens());
            merged = updateCard(card, pan, hpan, par);
        }
        manageAndEncryptTokens(card, readQueue.getTokens());
        log.info("Merged tokens: " + card.getTokens().stream().map(TkmCardToken::getHtoken).collect(Collectors.joining(", ")));
        citizenCardRepository.save(citizenCard);
        writeOnQueueIfComplete(citizenCard, oldTokens, merged);
    }

    private TkmCitizenCard createCitizenCard(String taxCode, String hpan, String par, String pan, CircuitEnum circuit) {
        TkmCard card = getOrCreateCard(hpan, par, pan, circuit);
        TkmCitizen citizen = getOrCreateCitizen(taxCode);
        return TkmCitizenCard.builder()
                .citizen(citizen)
                .card(card)
                .creationDate(Instant.now())
                .build();
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

    private TkmCard createCard(String pan, String hpan, String par, CircuitEnum circuit) {
        return TkmCard.builder()
                .circuit(circuit)
                .pan(cryptoService.encryptNullable(pan))
                .hpan(hpan)
                .par(par)
                .tokens(new HashSet<>())
                .creationDate(Instant.now())
                .build();
    }

    private TkmCitizenCard findCitizenCard(String taxCode, String hpan, String par) {
        TkmCitizenCard citizenCard = null;
        if (hpan != null) {
            log.info("Searching card for taxCode " + taxCode + " and hpan " + hpan);
            citizenCard = citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(taxCode, hpan);
        }
        if (citizenCard == null && par != null) {
            log.info("Card not found by hpan, searching by par " + par);
            citizenCard = citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(taxCode, par);
        }
        return citizenCard;
    }

    private TkmCard getOrCreateCard(String hpan, String par, String pan, CircuitEnum circuit) {
        TkmCard card = null;
        if (hpan != null) {
            log.info("Searching for card with hpan " + hpan);
            card = cardRepository.findByHpan(hpan);
        }
        if (card == null && par != null) {
            log.info("Card not found by hpan, searching by par " + par);
            card = cardRepository.findByPar(par);
        }
        if (card == null) {
            log.info("Card not found on database, creating new one");
            card = createCard(pan, hpan, par, circuit);
        }
        return card;
    }

    private boolean updateCard(TkmCard foundCard, String pan, String hpan, String par) {
        TkmCard preexistingCard = null;
        boolean toMerge = false;
        if (par != null && foundCard.getPar() == null) {
            preexistingCard = cardRepository.findByPar(par);
            foundCard.setPar(par);
            foundCard.setLastUpdateDate(Instant.now());
            toMerge = true;
        } else if (hpan != null && foundCard.getHpan() == null) {
            preexistingCard = cardRepository.findByHpan(hpan);
            foundCard.setPan(pan);
            foundCard.setHpan(hpan);
            foundCard.setLastUpdateDate(Instant.now());
            toMerge = true;
        }
        if (preexistingCard != null) {
            log.info("Preexisting card found with " + (par != null ? "par " + par : "hpan " + hpan) + ", merging");
            mergeTokens(preexistingCard.getTokens(), foundCard.getTokens());
            cardRepository.delete(preexistingCard);
        }
        return toMerge;
    }

    private void manageAndEncryptTokens(TkmCard card, List<ReadQueueToken> readQueueTokens) {
        if (readQueueTokens == null) {
            return;
        }
        Set<TkmCardToken> newTokens = queueTokensToEncryptedTkmTokens(card, readQueueTokens);
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

    private Set<TkmCardToken> queueTokensToEncryptedTkmTokens(TkmCard card, List<ReadQueueToken> readQueueTokens) {
        return readQueueTokens.stream().map(t -> TkmCardToken.builder()
                .card(card)
                .token(cryptoService.encrypt(t.getToken()))
                .htoken(StringUtils.isNotBlank(t.getHToken()) ? t.getHToken() : callRtdForHash(t.getToken()))
                .creationDate(Instant.now())
                .build()
        ).collect(Collectors.toSet());
    }

    private void writeOnQueueIfComplete(TkmCitizenCard citizenCard, Set<TkmCardToken> oldTokens, boolean merged) {
        TkmCard card = citizenCard.getCard();
        if (StringUtils.isAnyBlank(card.getPan(), card.getPar())) {
            log.info("Card missing pan or par, not writing on queue");
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
                    citizenCard.getCitizen().getTaxCode(),
                    Instant.now(),
                    Collections.singleton(writeQueueCard)
            );
            producerService.sendMessage(writeQueue);
        } catch (Exception e) {
            log.error(e);
            throw new CardException(MESSAGE_WRITE_FAILED);
        }
    }

    private Set<WriteQueueToken> getTokensDiff(Set<TkmCardToken> oldTokens, Set<TkmCardToken> newTokens, boolean merged) {
        return merged ?
                oldTokens.stream().map(WriteQueueToken::new).collect(Collectors.toSet())
                : newTokens.stream().filter(t -> t.isDeleted() || !oldTokens.contains(t)).map(WriteQueueToken::new).collect(Collectors.toSet());
    }

    private boolean getConsentForCard(TkmCard card, String taxCode) {
        log.info("Calling Consent Manager for card with taxCode " + taxCode + " and hpan " + card.getHpan());
        try {
            ConsentResponse consentResponse = consentClient.getConsent(taxCode, card.getHpan(), null);
            return consentResponse.cardHasConsent(card.getHpan());
        } catch (FeignException fe) {
            if (fe.status() == HttpStatus.NOT_FOUND.value()) {
                log.info("Consent not found for card");
                return false;
            }
            log.error(fe);
            throw new CardException(CALL_TO_CONSENT_MANAGER_FAILED);
        } catch (Exception e) {
            log.error(e);
            throw new CardException(CALL_TO_CONSENT_MANAGER_FAILED);
        }
    }

}
