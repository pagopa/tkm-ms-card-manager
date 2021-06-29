package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.WalletsHashingEvaluationInput;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCitizenCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueueToken;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenCardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.CardService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.CALL_TO_RTD_FAILED;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.INCONSISTENT_MESSAGE;

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
        if (StringUtils.isNotBlank(par) && CollectionUtils.isNotEmpty(tokens)) {
            checkCollectionSingleton(tokens);
            manageParAndToken(par, circuit, tokens);
        } else if (StringUtils.isNoneBlank(par, hpan)) {
            manageParAndHpan(par, hpan, circuit);
        } else if (CollectionUtils.isNotEmpty(tokens)) {
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
        TkmCard cardByHpanAndPar = cardRepository.findByHpanAndPar(hpan, par);
        if (cardByHpanAndPar != null) {
            return;
        }
        TkmCard cardByHpan = cardRepository.findByHpan(hpan);
        TkmCard cardByPar = cardRepository.findByPar(par);
        if (cardByHpan != null) {
            cardByHpan.setPar(par);
            if (cardByPar != null) {
                for (TkmCardToken t : cardByPar.getTokens()) {
                    t.setCard(cardByHpan);
                }
                updateCitizenCardAfterMerge(cardByHpan, cardByPar);
                cardRepository.delete(cardByPar);
            }
            cardRepository.save(cardByHpan);
        } else if (cardByPar != null) {
            cardByPar.setHpan(hpan);
            cardRepository.save(cardByPar);
        } else {
            TkmCard card = TkmCard.builder().hpan(hpan).par(par).circuit(circuit).build();
            cardRepository.save(card);
        }
    }

    private void updateCitizenCardAfterMerge(TkmCard survivingCard, TkmCard deletedCard) {
        List<TkmCitizenCard> citizenCards = citizenCardRepository.findByCardId(deletedCard.getId());
        citizenCards.forEach(c -> c.setCard(survivingCard));
        citizenCardRepository.saveAll(citizenCards);
    }

    private void manageOnlyToken(List<ReadQueueToken> tokens, CircuitEnum circuit) {
        ReadQueueToken readQueueToken = tokens.get(0);
        String token = readQueueToken.getToken();
        String htoken = getHtoken(readQueueToken.getHToken(), token);
        Optional<TkmCardToken> byHtokenAndDeletedFalse = cardTokenRepository.findByHtokenAndDeletedFalse(htoken);
        if (!byHtokenAndDeletedFalse.isPresent()) {
            log.debug("Adding htoken:" + htoken);
            TkmCard tkmCard = TkmCard.builder().circuit(circuit).build();
            TkmCardToken build = TkmCardToken.builder().htoken(htoken).token(cryptoService.encrypt(token)).card(tkmCard).build();
            tkmCard.setTokens(new HashSet<>(Collections.singleton(build)));
            cardRepository.save(tkmCard);
        } else {
            log.debug("Skip: Card Already Updated");
        }
    }

    private void manageParAndToken(String par, CircuitEnum circuit, List<ReadQueueToken> tokens) {
        ReadQueueToken readQueueToken = tokens.get(0);
        String token = readQueueToken.getToken();
        log.debug("manageParAndToken with par " + par);
        String htoken = getHtoken(readQueueToken.getHToken(), token);
        TkmCardToken byHtokenAndCardIsNull = cardTokenRepository.findByHtokenAndDeletedFalse(htoken)
                .orElse(TkmCardToken.builder().htoken(htoken).token(cryptoService.encrypt(token)).build());

        //Looking for the row with the par or with the token. If they exist I'll merge them
        TkmCard cardToSave = TkmCard.builder().par(par).circuit(circuit).build();
        TkmCard tokenCard = byHtokenAndCardIsNull.getCard();
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

        byHtokenAndCardIsNull.setCard(cardToSave);
        cardToSave.getTokens().add(byHtokenAndCardIsNull);

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

}
