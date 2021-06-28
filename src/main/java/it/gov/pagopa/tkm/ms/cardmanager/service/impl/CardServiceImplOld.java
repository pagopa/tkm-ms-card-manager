package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.WalletsHashingEvaluationInput;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueueToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueueToken;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.CardService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.CALL_TO_RTD_FAILED;

@Log4j2
public class CardServiceImplOld implements CardService {

    @Autowired
    private CardRepository cardRepository;

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
    public void updateOrCreateCard(ReadQueue readQueue, boolean fromIssuer) {
        String taxCode = readQueue.getTaxCode();
        if (StringUtils.isBlank(taxCode)) {
//            manageParUpdateAndAcquirerToken(readQueue);
        } else if (readQueue.getTokens() == null) {
//            managePmCard(readQueue);
        } else {
            //gestire come adesso
        }

        String par = readQueue.getPar();
//        String pan = readQueue.getPan();
//        String hpan = (readQueue.getHpan() == null && pan != null) ? callRtdForHash(pan) : readQueue.getHpan();
//        Set<TkmCardToken> oldTokens = new HashSet<>();
//        TkmCard card = findCard(taxCode, hpan, par);
//        boolean merged = false;
//        if (card == null) {
//            log.info("Card not found on database, creating new one");
//            card = createCard(taxCode, pan, hpan, par, readQueue.getCircuit());
//        } else {
//            log.info("Card found on database, updating");
//            oldTokens.addAll(card.getTokens());
//            merged = updateCard(card, pan, hpan, par);
//        }
//
//        manageAndEncryptTokens(card, readQueue.getTokens(), fromIssuer);
//        log.info("Merged tokens: " + card.getTokens().
//
//                stream().
//
//                map(TkmCardToken::getHtoken).
//
//                collect(Collectors.joining(", ")));
//        cardRepository.save(card);
//
//        writeOnQueueIfComplete(card, oldTokens, merged);

    }

//    private void managePmCard(ReadQueue readQueue) {
//        //todo controllo sula struttura
//        String taxCode = readQueue.getTaxCode();
//        String hpan = readQueue.getHpan();
//        String par = readQueue.getPar();
//        String pan = readQueue.getPan();
//        TkmCard card = findCard(taxCode, hpan, par);
//        if (card == null) {
//            card = TkmCard.builder()
//                    .taxCode(taxCode)
//                    .deleted(false)
//                    .build();
//        }
//        boolean wasAlreadyCompleted = card.isCompleteToSendBpd();
//        card.setLastUpdateDate(Instant.now());
//        card.setHpan(StringUtils.firstNonBlank(card.getHpan(), callRtdForHash(hpan)));
//        card.setPan(StringUtils.firstNonBlank(card.getPan(), cryptoService.encrypt(pan)));
//        card.setPar(StringUtils.firstNonBlank(card.getPar(), par));
//        card.setCircuit(ObjectUtils.firstNonNull(card.getCircuit(), readQueue.getCircuit()));
//        cardRepository.save(card);
//        if (!wasAlreadyCompleted && card.isCompleteToSendBpd()) {
//            //todo ritornare tutti i token per essere mandati
//        }
//    }

//    private void manageParUpdateAndAcquirerToken(ReadQueue readQueue) {
//        String par = readQueue.getPar();
//        String hpan = readQueue.getHpan();
//        //Aggiungere controllo che non contenga più di 1 token
//        List<ReadQueueToken> tokens = readQueue.getTokens();
//        if (StringUtils.isNotBlank(par) && CollectionUtils.isNotEmpty(tokens)) {
//            ReadQueueToken readQueueToken = tokens.get(0);
//            TkmCard tkmCards = cardRepository.findByPar(par).orElse(TkmCard.builder().par(par).circuit(readQueue.getCircuit()).build());
//            String token = readQueueToken.getToken();
//            String htoken = getHtoken(token, readQueueToken.getHToken());
//            List<TkmCardToken> byHtokenAndCardIsNull = cardTokenRepository.findByHtoken(htoken).orElse(new ArrayList<>());
//            Set<TkmCardToken> tkmCardTokensUnique = new HashSet<>(byHtokenAndCardIsNull);
//            tkmCardTokensUnique.add(TkmCardToken.builder().htoken(htoken).token(cryptoService.encrypt(token)).build());
//            tkmCards.setTokens(tkmCardTokensUnique);
//            cardRepository.save(tkmCards);
//            //Selezione tutte quelle che non sono presenti nella lista unique  e quindi sono da eliminare
//            List<TkmCardToken> collect = byHtokenAndCardIsNull.stream().
//                    filter(t -> tkmCardTokensUnique.stream().noneMatch(tu -> t.getId().equals(tu.getId()))).collect(Collectors.toList());
//            cardTokenRepository.deleteAll(collect);
//        } else if (StringUtils.isNoneBlank(par, hpan)) {
//            //Cerco la carta con hpan e quella con par e poi mergio sia la carte che i token.
//            //caso par hpan
//        } else if (StringUtils.isBlank(par) && CollectionUtils.isNotEmpty(tokens)) {
//            //caso solo aggiunta token
//        } else {
//            //messaggio inconsistente
//        }
//
//    }

    private void setParTokenCircuit(TkmCard c, ReadQueue readQueue) {
        c.setCircuit(ObjectUtils.firstNonNull(c.getCircuit(), readQueue.getCircuit()));
        c.setPar(StringUtils.firstNonBlank(c.getPar(), readQueue.getPar()));
        List<ReadQueueToken> tokens = readQueue.getTokens();
        if (tokens != null) {
            for (ReadQueueToken readQueueToken : tokens) {
                String token = readQueueToken.getToken();
                String hToken = StringUtils.firstNonBlank(readQueueToken.getHToken(), callRtdForHash(readQueueToken.getHToken()));
                tokens.add(new ReadQueueToken(token, hToken));
            }
        }
    }

//    private TkmCard findCard(String taxCode, String hpan, String par) {
//        TkmCard card = null;
//        if (hpan != null) {
//            log.info("Searching card for taxCode " + taxCode + " and hpan " + hpan);
//            card = cardRepository.findByTaxCodeAndHpanAndDeletedFalse(taxCode, hpan);
//        }
//        if (card == null && par != null) {
//            log.info("Card not found by hpan, searching by par " + par);
//            card = cardRepository.findByTaxCodeAndParAndDeletedFalse(taxCode, par);
//        }
//        return card;
//    }

//    private TkmCard createCard(String taxCode, String pan, String hpan, String par, CircuitEnum circuit) {
//        return TkmCard.builder()
//                .taxCode(taxCode)
//                .circuit(circuit)
//                .pan(cryptoService.encryptNullable(pan))
//                .hpan(hpan)
//                .par(par)
//                .tokens(new HashSet<>())
//                .build();
//    }

//    private boolean updateCard(TkmCard foundCard, String pan, String hpan, String par) {
//        TkmCard preexistingCard = null;
//        String taxCode = foundCard.getTaxCode();
//        boolean toMerge = false;
//        if (par != null && foundCard.getPar() == null) {
//            preexistingCard = cardRepository.findByTaxCodeAndParAndDeletedFalse(taxCode, par);
//            foundCard.setPar(par);
//            toMerge = true;
//        } else if (hpan != null && foundCard.getHpan() == null) {
//            preexistingCard = cardRepository.findByTaxCodeAndHpanAndDeletedFalse(taxCode, hpan);
//            foundCard.setPan(pan);
//            foundCard.setHpan(hpan);
//            toMerge = true;
//        }
//        if (preexistingCard != null) {
//            log.info("Preexisting card found with " + (par != null ? "par " + par : "hpan " + hpan) + ", merging");
//            mergeTokens(preexistingCard.getTokens(), foundCard.getTokens());
//            cardRepository.delete(preexistingCard);
//        }
//        return toMerge;
//    }

    private void manageAndEncryptTokens(TkmCard card, List<ReadQueueToken> readQueueTokens, boolean fromIssuer) {
        if (readQueueTokens == null) {
            return;
        }
        Set<TkmCardToken> newTokens = queueTokensToEncryptedTkmTokens(card, readQueueTokens);
        if (CollectionUtils.isEmpty(newTokens) || fromIssuer) {
            mergeTokens(card.getTokens(), newTokens);
        }
        card.getTokens().addAll(newTokens);
    }

    private void mergeTokens(Set<TkmCardToken> oldTokens, Set<TkmCardToken> newTokens) {
        oldTokens.stream().filter(t -> !newTokens.contains(t)).forEach(t -> t.setDeleted(true));
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

    private Set<TkmCardToken> queueTokensToEncryptedTkmTokens(TkmCard card, List<ReadQueueToken> readQueueTokens) {
        return readQueueTokens.stream().map(t -> TkmCardToken.builder()
//                .card(card)
                .token(cryptoService.encrypt(t.getToken()))
                .htoken(StringUtils.isNotBlank(t.getHToken()) ? t.getHToken() : callRtdForHash(t.getToken()))
                .build()
        ).collect(Collectors.toSet());
    }

//    private void writeOnQueueIfComplete(TkmCard card, Set<TkmCardToken> oldTokens, boolean merged) {
//        if (StringUtils.isAnyBlank(card.getPan(), card.getPar(), card.getTaxCode())) {
//            log.info("Card missing pan, par or taxCode, not writing on queue");
//            return;
//        }
//        if (!getConsentForCard(card)) {
//            return;
//        }
//        try {
//            WriteQueueCard writeQueueCard = new WriteQueueCard(
//                    card.getHpan(),
//                    INSERT_UPDATE,
//                    card.getPar(),
//                    getTokensDiff(oldTokens, card.getTokens(), merged)
//            );
//            WriteQueue writeQueue = new WriteQueue(
//                    card.getTaxCode(),
//                    Instant.now(),
//                    Collections.singleton(writeQueueCard)
//            );
//            producerService.sendMessage(writeQueue);
//        } catch (Exception e) {
//            log.error(e);
//            throw new CardException(MESSAGE_WRITE_FAILED);
//        }
//    }

    private Set<WriteQueueToken> getTokensDiff(Set<TkmCardToken> oldTokens, Set<TkmCardToken> newTokens, boolean merged) {
        return merged ?
                oldTokens.stream().map(WriteQueueToken::new).collect(Collectors.toSet())
                : newTokens.stream().filter(t -> t.isDeleted() || !oldTokens.contains(t)).map(WriteQueueToken::new).collect(Collectors.toSet());
    }

//    private boolean getConsentForCard(TkmCard card) {
//        log.info("Calling Consent Manager for card with taxCode " + card.getTaxCode() + " and hpan " + card.getHpan());
//        try {
//            ConsentResponse consentResponse = consentClient.getConsent(card.getTaxCode(), card.getHpan(), null);
//            return consentResponse.cardHasConsent(card.getHpan());
//        } catch (FeignException fe) {
//            if (fe.status() == HttpStatus.NOT_FOUND.value()) {
//                log.info("Consent not found for card");
//                return false;
//            }
//            log.error(fe);
//            throw new CardException(CALL_TO_CONSENT_MANAGER_FAILED);
//        } catch (Exception e) {
//            log.error(e);
//            throw new CardException(CALL_TO_CONSENT_MANAGER_FAILED);
//        }
//    }


    private String getHtoken(String htoken, String token) {
        if (StringUtils.isNotBlank(htoken))
            return htoken;
        return callRtdForHash(token);
    }

}
