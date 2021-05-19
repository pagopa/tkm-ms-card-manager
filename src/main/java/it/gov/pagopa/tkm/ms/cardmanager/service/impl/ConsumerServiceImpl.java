package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.fasterxml.jackson.databind.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.hash.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.hash.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.crypto.*;
import it.gov.pagopa.tkm.ms.cardmanager.exception.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.Constants.*;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.*;

@Service
public class ConsumerServiceImpl implements ConsumerService {

    @Autowired
    private PgpUtils pgpUtils;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ApimClient apimClient;

    @Override
    @KafkaListener(topics = TKM_READ_TOKEN_PAR_PAN_TOPIC)
    public void consume(String message) throws Exception {
        ReadQueue readQueue = mapper.readValue(pgpUtils.decrypt(message), ReadQueue.class);
        updateOrCreateCard(readQueue);
    }

    private void updateOrCreateCard(ReadQueue readQueue) {
        TkmCard card;
        String taxCode = readQueue.getTaxCode();
        String pan = readQueue.getPan();
        String hpan = readQueue.getHpan();
        String par = readQueue.getPar();
        if (hpan != null) {
            card = cardRepository.findByTaxCodeAndHpan(taxCode, hpan);
        } else if (pan != null) {
            hpan = callApimForHash(pan);
            card = cardRepository.findByTaxCodeAndHpan(taxCode, hpan);
        } else if (par != null) {
            card = cardRepository.findByTaxCodeAndPar(taxCode, par);
        } else {
            throw new CardException(PAN_NOT_FOUND);
        }
        if (card == null) {
            card = new TkmCard()
                    .setTaxCode(taxCode)
                    .setCircuit(readQueue.getCircuit())
                    .setHpan(hpan)
                    .setPan(pan);
        }
        card.setPar(par);
        Set<TkmCardToken> newTokens = queueTokensToTkmTokens(readQueue.getTokens(), card);
        Set<TkmCardToken> oldTokens = card.getTokens().stream()
                .filter(t -> !newTokens.contains(t))
                .map(t -> t.setDeleted(true)).collect(Collectors.toSet());
        newTokens.addAll(oldTokens);
        card.setTokens(newTokens);
        cardRepository.save(card);
    }

    private String callApimForHash(String pan) {
        return apimClient.getHash(new WalletsHashingEvaluationInput(pan)).getHashPan();
    }

    private Set<TkmCardToken> queueTokensToTkmTokens(List<Token> tokens, TkmCard card) {
        return tokens.stream().map(t -> new TkmCardToken()
                        .setCard(card)
                        .setToken(t.getToken())
                        .setHtoken(callApimForHash(t.getToken()))
        ).collect(Collectors.toSet());
    }

}
