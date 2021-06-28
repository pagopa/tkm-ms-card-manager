package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.WalletsHashingEvaluationInput;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueueToken;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.CardService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.CALL_TO_RTD_FAILED;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.INCONSISTENT_MESSAGE;

@Service
@Log4j2
public class CardServiceImpl implements CardService {

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
            manageParUpdateAndAcquirerToken(readQueue);
        } else if (readQueue.getTokens() == null) {
        } else {
            //gestire come adesso
        }

    }

    private void manageParUpdateAndAcquirerToken(ReadQueue readQueue) {
        String par = readQueue.getPar();
        String hpan = readQueue.getHpan();
        CircuitEnum circuit = readQueue.getCircuit();
        //TODO: Aggiungere controllo che non contenga più di 1 token
        List<ReadQueueToken> tokens = readQueue.getTokens();
        if (StringUtils.isNotBlank(par) && CollectionUtils.isNotEmpty(tokens)) {
            manageParAndToken(par, circuit, tokens);
        } else if (StringUtils.isNoneBlank(par, hpan)) {
            manageParAndHtoken(par, hpan, circuit);
        } else if (CollectionUtils.isNotEmpty(tokens)) {
            manageOnlyToken(tokens);
        } else {
            throw new CardException(INCONSISTENT_MESSAGE);
        }
    }

    private void manageParAndHtoken(String par, String hpan, CircuitEnum circuit) {
        TkmCard cardByHpan = cardRepository.findByHpanAndParNull(hpan);
        TkmCard cardByPar = cardRepository.findByParAndHpanNull(par);
        if (cardByHpan != null) {
            cardByHpan.setPar(par);
            if (cardByPar != null) {
                mergeTokens(cardByPar.getTokens(), cardByHpan.getTokens());
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

    private void manageOnlyToken(List<ReadQueueToken> tokens) {
        ReadQueueToken readQueueToken = tokens.get(0);
        String token = readQueueToken.getToken();
        String htoken = getHtoken(readQueueToken.getHToken(), token);
        Optional<TkmCardToken> byHtokenAndDeletedFalse = cardTokenRepository.findByHtokenAndDeletedFalse(htoken);
        if (!byHtokenAndDeletedFalse.isPresent()) {
            TkmCardToken build = TkmCardToken.builder().htoken(htoken).token(cryptoService.encrypt(token)).build();
            cardTokenRepository.save(build);
        }
    }

    private void manageParAndToken(String par, CircuitEnum circuit, List<ReadQueueToken> tokens) {
        ReadQueueToken readQueueToken = tokens.get(0);
        TkmCard tkmCards = cardRepository.findByPar(par).orElse(TkmCard.builder().par(par).circuit(circuit).build());
        String token = readQueueToken.getToken();
        String htoken = getHtoken(readQueueToken.getHToken(), token);
        TkmCardToken byHtokenAndCardIsNull = cardTokenRepository.findByHtokenAndDeletedFalse(htoken)
                .orElse(TkmCardToken.builder().htoken(htoken).token(cryptoService.encrypt(token)).build());
        tkmCards.getTokens().add(byHtokenAndCardIsNull);
        tkmCards.getTokens().forEach(t -> t.setCard(tkmCards));
        cardRepository.save(tkmCards);
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

    private String getHtoken(String htoken, String token) {
        if (StringUtils.isNotBlank(htoken))
            return htoken;
        return callRtdForHash(token);
    }

}
