package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.hash.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.hash.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.crypto.*;
import it.gov.pagopa.tkm.ms.cardmanager.exception.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.*;
import lombok.extern.log4j.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.*;

import javax.validation.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.Constants.*;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.*;
import static it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.CardActionEnum.*;

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

    @Autowired
    private ProducerServiceImpl producerService;

    @Override
    @KafkaListener(topics = TKM_READ_TOKEN_PAR_PAN_TOPIC)
    public void consume(String message) throws JsonProcessingException {
        String decryptedMessage;
        try {
            decryptedMessage = pgpUtils.decrypt(message);
        } catch (Exception e) {
            throw new CardException(MESSAGE_DECRYPTION_FAILED);
        }
        ReadQueue readQueue = mapper.readValue(decryptedMessage, ReadQueue.class);
        validateReadQueue(readQueue);
        updateOrCreateCard(readQueue);
    }

    private void validateReadQueue(ReadQueue readQueue) {
        if (!CollectionUtils.isEmpty(validator.validate(readQueue))) {
            throw new CardException(MESSAGE_VALIDATION_FAILED);
        }
    }

    private void updateOrCreateCard(ReadQueue readQueue) throws JsonProcessingException {
        String taxCode = readQueue.getTaxCode();
        String par = readQueue.getPar();
        String pan = readQueue.getPan();
        String hpan = (readQueue.getHpan() == null && pan != null) ? callApimForHash(pan) : readQueue.getHpan();
        TkmCard card = findCard(taxCode, hpan, par);
        Set<TkmCardToken> oldTokens = new HashSet<>();
        if (card == null) {
            card = createCard(taxCode, pan, hpan, par, readQueue.getCircuit());
        } else {
            oldTokens.addAll(card.getTokens());
            updateCard(card, pan, hpan, par);
        }
        manageTokens(card, readQueue.getTokens());
        cardRepository.save(card);
        writeOnQueueIfComplete(card, oldTokens);
    }

    private TkmCard findCard(String taxCode, String hpan, String par) {
        TkmCard card = null;
        if (hpan != null) {
            card = cardRepository.findByTaxCodeAndHpanAndDeletedFalse(taxCode, hpan);
        }
        if (card == null && par != null) {
            card = cardRepository.findByTaxCodeAndParAndDeletedFalse(taxCode, par);
        }
        return card;
    }

    private TkmCard createCard(String taxCode, String pan, String hpan, String par, CircuitEnum circuit) {
        return new TkmCard()
                .setTaxCode(taxCode)
                .setCircuit(circuit)
                .setPan(pan)
                .setHpan(hpan)
                .setPar(par);
    }

    private void updateCard(TkmCard foundCard, String pan, String hpan, String par) {
        TkmCard preexistingCard = null;
        if (par != null && foundCard.getPar() == null) {
            preexistingCard = cardRepository.findByTaxCodeAndParAndDeletedFalse(foundCard.getTaxCode(), par);
            foundCard.setPar(par);
        } else if (hpan != null && foundCard.getHpan() == null) {
            preexistingCard = cardRepository.findByTaxCodeAndHpanAndDeletedFalse(foundCard.getTaxCode(), hpan);
            foundCard.setPan(pan).setHpan(hpan);
        }
        if (preexistingCard != null) {
            mergeTokens(preexistingCard.getTokens(), foundCard.getTokens());
            cardRepository.delete(preexistingCard);
        }
    }

    private void manageTokens(TkmCard card, List<ReadQueueToken> readQueueTokens) {
        Set<TkmCardToken> newTokens = queueTokensToTkmTokens(card, readQueueTokens);
        mergeTokens(card.getTokens(), newTokens);
        card.getTokens().addAll(newTokens);
    }

    private void mergeTokens(Set<TkmCardToken> oldTokens, Set<TkmCardToken> newTokens) {
        oldTokens.stream().filter(t -> !newTokens.contains(t)).forEach(t -> t.setDeleted(true));
    }

    private String callApimForHash(String pan) {
        return apimClient.getHash(new WalletsHashingEvaluationInput(pan)).getHashPan();
    }

    private Set<TkmCardToken> queueTokensToTkmTokens(TkmCard card, List<ReadQueueToken> readQueueTokens) {
        return readQueueTokens.stream().map(t -> new TkmCardToken()
                        .setCard(card)
                        .setToken(t.getToken())
                        .setHtoken(StringUtils.isNotBlank(t.getHToken()) ? t.getHToken() : callApimForHash(t.getToken()))
        ).collect(Collectors.toSet());
    }

    private void writeOnQueueIfComplete(TkmCard card, Set<TkmCardToken> oldTokens) throws JsonProcessingException {
        if (StringUtils.isAnyBlank(card.getPan(), card.getPar())) {
            return;
        }

        WriteQueueCard writeQueueCard = new WriteQueueCard(
                card.getHpan(),
                card.isDeleted() ? REVOKE : INSERT_UPDATE,
                card.getPar(),
                getTokensDiff(oldTokens, card.getTokens())
        );
        WriteQueue writeQueue = new WriteQueue(
            card.getTaxCode(),
            Instant.now(),
            Collections.singleton(writeQueueCard)
        );
        producerService.sendMessage(writeQueue);
    }

    private Set<WriteQueueToken> getTokensDiff(Set<TkmCardToken> oldTokens, Set<TkmCardToken> newTokens) {
        return newTokens.stream().filter(t -> t.isDeleted() || !oldTokens.contains(t)).map(WriteQueueToken::new).collect(Collectors.toSet());
    }

}
