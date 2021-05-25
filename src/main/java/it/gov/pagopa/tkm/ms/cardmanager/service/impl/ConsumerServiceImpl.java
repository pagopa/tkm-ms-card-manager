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
import lombok.extern.log4j.*;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.openpgp.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.*;

import javax.validation.*;
import java.util.*;
import java.util.stream.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.Constants.*;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.*;

@Service
@Log4j2
public class ConsumerServiceImpl implements ConsumerService {

    @Autowired
    private PgpUtils pgpUtils;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ApimClient apimClient;

    @Autowired
    private Validator validator;

    @Override
    @KafkaListener(topics = TKM_READ_TOKEN_PAR_PAN_TOPIC)
    public void consume(String message) throws Exception {
        try {
            ReadQueue readQueue = mapper.readValue(pgpUtils.decrypt(message), ReadQueue.class);
            validateReadQueue(readQueue);
            updateOrCreateCard(readQueue);
        } catch (PGPException e) {
            log.error("Could not decrypt PGP message: " + message);
        }
    }

    private void validateReadQueue(ReadQueue readQueue) {
        if (!CollectionUtils.isEmpty(validator.validate(readQueue))) {
            throw new CardException(REQUEST_VALIDATION_FAILED);
        }
    }

    private void updateOrCreateCard(ReadQueue readQueue) {
        validator.validate(readQueue);
        String taxCode = readQueue.getTaxCode();
        String pan = readQueue.getPan();
        String hpan = readQueue.getHpan();
        if (hpan == null && pan != null) {
            hpan = callApimForHash(pan);
        }
        String par = readQueue.getPar();
        List<Token> tokens = readQueue.getTokens();
        TkmCard card = findCard(taxCode, hpan, par);
        if (card == null) {
            card = new TkmCard()
                    .setTaxCode(taxCode)
                    .setCircuit(readQueue.getCircuit());
        }
        updateCard(card, pan, hpan, par, tokens);
        cardRepository.save(card);
    }

    private TkmCard findCard(String taxCode, String hpan, String par) {
        TkmCard card;
        if (hpan != null) {
            card = cardRepository.findByTaxCodeAndHpanAndDeletedFalse(taxCode, hpan);
        } else if (par != null) {
            card = cardRepository.findByTaxCodeAndParAndDeletedFalse(taxCode, par);
        } else {
            throw new CardException(PAN_MISSING);
        }
        return card;
    }

    private void updateCard(TkmCard card, String pan, String hpan, String par, List<Token> tokens) {
        TkmCard existingCard = null;
        if (par != null && card.getPar() == null) {
            existingCard = cardRepository.findByTaxCodeAndParAndDeletedFalse(card.getTaxCode(), par);
            card.setPar(par);
        } else if (pan != null && card.getPan() == null) {
            existingCard = cardRepository.findByTaxCodeAndHpanAndDeletedFalse(card.getTaxCode(), hpan);
            card.setPan(pan).setHpan(hpan);
        }
        if (existingCard != null) {
            mergeTokens(existingCard.getTokens(), card.getTokens());
            cardRepository.delete(existingCard);
        }
        Set<TkmCardToken> newTokens = queueTokensToTkmTokens(tokens, card);
        mergeTokens(card.getTokens(), newTokens);
        card.getTokens().addAll(newTokens);
    }

    private void mergeTokens(Set<TkmCardToken> oldTokens, Set<TkmCardToken> newTokens) {
        oldTokens.stream().filter(t -> !newTokens.contains(t)).forEach(t -> t.setDeleted(true));
    }

    private String callApimForHash(String pan) {
        return apimClient.getHash(new WalletsHashingEvaluationInput(pan)).getHashPan();
    }

    private Set<TkmCardToken> queueTokensToTkmTokens(List<Token> tokens, TkmCard card) {
        return tokens.stream().map(t -> new TkmCardToken()
                        .setCard(card)
                        .setToken(t.getToken())
                        .setHtoken(StringUtils.firstNonBlank(t.getHToken(), callApimForHash(t.getToken())))
        ).collect(Collectors.toSet());
    }

}
