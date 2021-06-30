package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import feign.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.WalletsHashingEvaluationInput;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCitizenCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueueToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenCardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.CardService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.*;

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
            manageParUpdateAndAcquirerToken(readQueue);
        } else if (readQueue.getTokens() == null) {
            log.info("NOT IMPLEMENTED YET");
        } else {
            //TODO: gestire come adesso
            log.info("NOT IMPLEMENTED YET");
        }
    }

    private void manageParUpdateAndAcquirerToken(ReadQueue readQueue) {
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
            if (cardByPar != null) {
                log.debug("Also found card by par " + par + ", merging it into card found by hpan");
                log.trace(cardByPar);
                for (TkmCardToken t : cardByPar.getTokens()) {
                    t.setCard(cardByHpan);
                }
                updateCitizenCardAfterMerge(cardByHpan, cardByPar);
                cardRepository.delete(cardByPar);
            }
            cardRepository.save(cardByHpan);
        } else if (cardByPar != null) {
            log.debug("Found card by par " + par + ", updating");
            cardByPar.setHpan(hpan);
            cardRepository.save(cardByPar);
        } else {
            log.debug("No existing cards found, creating one");
            TkmCard card = TkmCard.builder().hpan(hpan).par(par).circuit(circuit).build();
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
        String htoken = getHtoken(readQueueToken.getHToken(), token);
        Optional<TkmCardToken> byHtokenAndDeletedFalse = cardTokenRepository.findByHtokenAndDeletedFalse(htoken);
        if (!byHtokenAndDeletedFalse.isPresent()) {
            log.debug("Adding htoken:" + htoken);
            TkmCard fakeCard = TkmCard.builder().circuit(circuit).build();
            TkmCardToken build = TkmCardToken.builder().htoken(htoken).token(cryptoService.encrypt(token)).card(fakeCard).build();
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
        TkmCardToken byHtoken = cardTokenRepository.findByHtokenAndDeletedFalse(htoken)
                .orElse(TkmCardToken.builder().htoken(htoken).token(cryptoService.encrypt(token)).build());
        //Looking for the row with the par or with the token. If they exist I'll merge them
        TkmCard cardToSave = TkmCard.builder().par(par).circuit(circuit).build();
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
            deleteIfNotNull(tokenCard);
            cardToSave = parCard;
        } else if (tokenCard != null) {
            cardToSave = tokenCard;
            cardToSave.setPar(par);
        }
        byHtoken.setCard(cardToSave);
        cardToSave.getTokens().add(byHtoken);
        cardRepository.save(cardToSave);
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

    //TODO USE
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
