package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.WalletsHashingEvaluationInput;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (CollectionUtils.isNotEmpty(tokens)) {
            ReadQueueToken readQueueToken = tokens.get(0);
            String token = readQueueToken.getToken();
            String htoken = getHtoken(token, readQueueToken.getHToken());
            TkmCardToken existingToken = cardTokenRepository.findByHtokenAndDeletedFalse(htoken)
                    .orElse(TkmCardToken.builder().htoken(htoken).token(cryptoService.encrypt(token)).build());
            cardTokenRepository.save(existingToken);
            if (StringUtils.isNotBlank(par)) {
                TkmCard card = cardRepository.findByParAndHpanNull(par);
                if (card == null) {
                    card = TkmCard.builder().par(par).circuit(circuit).build();
                }
                card.getTokens().add(existingToken);
                cardRepository.save(card);
            }
        } else if (StringUtils.isNoneBlank(par, hpan)) {
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
        } else {
            throw new CardException(INCONSISTENT_MESSAGE);
        }
    }

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

    private TkmCard findCard(String taxCode, String hpan, String par) {
        TkmCard card = null;
        if (hpan != null) {
            log.info("Searching card for taxCode " + taxCode + " and hpan " + hpan);
            card = cardRepository.findByTaxCodeAndHpanAndDeletedFalse(taxCode, hpan);
        }
        if (card == null && par != null) {
            log.info("Card not found by hpan, searching by par " + par);
            card = cardRepository.findByTaxCodeAndParAndDeletedFalse(taxCode, par);
        }
        return card;
    }

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
                .card(card)
                .token(cryptoService.encrypt(t.getToken()))
                .htoken(StringUtils.isNotBlank(t.getHToken()) ? t.getHToken() : callRtdForHash(t.getToken()))
                .build()
        ).collect(Collectors.toSet());
    }


    private Set<WriteQueueToken> getTokensDiff(Set<TkmCardToken> oldTokens, Set<TkmCardToken> newTokens, boolean merged) {
        return merged ?
                oldTokens.stream().map(WriteQueueToken::new).collect(Collectors.toSet())
                : newTokens.stream().filter(t -> t.isDeleted() || !oldTokens.contains(t)).map(WriteQueueToken::new).collect(Collectors.toSet());
    }


    private String getHtoken(String htoken, String token) {
        if (StringUtils.isNotBlank(htoken))
            return htoken;
        return callRtdForHash(token);
    }

}
