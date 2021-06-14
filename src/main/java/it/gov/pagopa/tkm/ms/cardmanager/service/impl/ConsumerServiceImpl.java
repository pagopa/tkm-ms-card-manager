package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import it.gov.pagopa.tkm.ms.cardmanager.client.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.rtd.model.request.WalletsHashingEvaluationInput;
import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentResponse;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete.DeleteQueueMessage;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueueToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueueCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueueToken;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.ConsumerService;
import it.gov.pagopa.tkm.ms.cardmanager.service.DeleteCardService;
import it.gov.pagopa.tkm.service.PgpUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.*;
import static it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.CardActionEnum.INSERT_UPDATE;

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
    private RtdHashingClient rtdHashingClient;

    @Autowired
    private Validator validator;

    @Autowired
    private ConsentClient consentClient;

    @Autowired
    private ProducerServiceImpl producerService;

    @Value("${keyvault.apimSubscriptionTkmRtd}")
    private String apimRtdSubscriptionKey;

    @Autowired
    private DeleteCardService deleteCardService;

    @PostConstruct
    public void initConsumerServiceImpl() {
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
    }

    @Override
    @KafkaListener(topics = "#{'${spring.kafka.topics.read-queue.name}'}",
            groupId = "${spring.kafka.topics.read-queue.group-id}",
            clientIdPrefix = "${spring.kafka.topics.read-queue.client-id}",
            properties = {"security.sasl.jaas.config:${keyvault.cardMEventhubReadSaslJaasConfig}"},
            concurrency = "${spring.kafka.topics.read-queue.concurrency}")
    public void consume(String message) throws JsonProcessingException {
        log.debug("Reading message from queue: " + message);
        String decryptedMessage;
        try {
            decryptedMessage = pgpUtils.decrypt(message);
        } catch (Exception e) {
            log.error(e);
            throw new CardException(MESSAGE_DECRYPTION_FAILED);
        }
        log.trace("Decrypted message from queue: " + decryptedMessage);
        ReadQueue readQueue = mapper.readValue(decryptedMessage, ReadQueue.class);
        validateMessage(readQueue);
        updateOrCreateCard(readQueue);
    }

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.delete-queue.name}",
            groupId = "${spring.kafka.topics.delete-queue.group-id}",
            clientIdPrefix = "${spring.kafka.topics.delete-queue.client-id}",
            properties = {"security.sasl.jaas.config:${keyvault.cardMEventhubDeleteSaslJaasConfig}"},
            concurrency = "${spring.kafka.topics.delete-queue.concurrency}")
    public void consumeDelete(String message) {
        log.debug("Delete message not parsed " + message);
        try {
            DeleteQueueMessage deleteQueueMessage = mapper.readValue(message, DeleteQueueMessage.class);
            log.debug("Delete message  parsed " + deleteQueueMessage);
            validateMessage(deleteQueueMessage);
            deleteCardService.deleteCard(deleteQueueMessage);
        } catch (Exception e) {
            e.printStackTrace();
            //todo fixit
        }
    }

    private <T> void validateMessage(T readQueue) {
        Set<ConstraintViolation<T>> violations = validator.validate(readQueue);
        if (!CollectionUtils.isEmpty(violations)) {
            log.error("Validation errors: " + violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; ")));
            throw new CardException(MESSAGE_VALIDATION_FAILED);
        }
    }

    private void updateOrCreateCard(ReadQueue readQueue) {
        String taxCode = readQueue.getTaxCode();
        String par = readQueue.getPar();
        String pan = readQueue.getPan();
        String hpan = (readQueue.getHpan() == null && pan != null) ? callRtdForHash(pan) : readQueue.getHpan();
        TkmCard card = findCard(taxCode, hpan, par);
        Set<TkmCardToken> oldTokens = new HashSet<>();
        boolean merged = false;
        if (card == null) {
            log.info("Card not found on database, creating new one");
            card = createCard(taxCode, pan, hpan, par, readQueue.getCircuit());
        } else {
            log.info("Card found on database, updating");
            oldTokens.addAll(card.getTokens());
            merged = updateCard(card, pan, hpan, par);
        }
        manageTokens(card, readQueue.getTokens());
        log.info("Merged tokens: " + card.getTokens().stream().map(TkmCardToken::getHtoken).collect(Collectors.joining(", ")));
        cardRepository.save(card);
        writeOnQueueIfComplete(card, oldTokens, merged);
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

    private TkmCard createCard(String taxCode, String pan, String hpan, String par, CircuitEnum circuit) {
        return new TkmCard()
                .setTaxCode(taxCode)
                .setCircuit(circuit)
                .setPan(pan)
                .setHpan(hpan)
                .setPar(par);
    }

    private boolean updateCard(TkmCard foundCard, String pan, String hpan, String par) {
        TkmCard preexistingCard = null;
        String taxCode = foundCard.getTaxCode();
        boolean toMerge = false;
        if (par != null && foundCard.getPar() == null) {
            preexistingCard = cardRepository.findByTaxCodeAndParAndDeletedFalse(taxCode, par);
            foundCard.setPar(par);
            toMerge = true;
        } else if (hpan != null && foundCard.getHpan() == null) {
            preexistingCard = cardRepository.findByTaxCodeAndHpanAndDeletedFalse(taxCode, hpan);
            foundCard.setPan(pan).setHpan(hpan);
            toMerge = true;
        }
        if (preexistingCard != null) {
            log.info("Preexisting card found with " + (par != null ? "par " + par : "hpan " + hpan) + ", merging");
            mergeTokens(preexistingCard.getTokens(), foundCard.getTokens());
            cardRepository.delete(preexistingCard);
        }
        return toMerge;
    }

    private void manageTokens(TkmCard card, List<ReadQueueToken> readQueueTokens) {
        Set<TkmCardToken> newTokens = queueTokensToTkmTokens(card, readQueueTokens);
        mergeTokens(card.getTokens(), newTokens);
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

    private Set<TkmCardToken> queueTokensToTkmTokens(TkmCard card, List<ReadQueueToken> readQueueTokens) {
        return readQueueTokens.stream().map(t -> new TkmCardToken()
                .setCard(card)
                .setToken(t.getToken())
                .setHtoken(StringUtils.isNotBlank(t.getHToken()) ? t.getHToken() : callRtdForHash(t.getToken()))
        ).collect(Collectors.toSet());
    }

    private void writeOnQueueIfComplete(TkmCard card, Set<TkmCardToken> oldTokens, boolean merged) {
        if (StringUtils.isAnyBlank(card.getPan(), card.getPar())) {
            log.info("Card missing pan or par, not writing on queue");
            return;
        }
        if (!getConsentForCard(card)) {
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
                    card.getTaxCode(),
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

    private boolean getConsentForCard(TkmCard card) {
        log.info("Calling Consent Manager for card with taxCode " + card.getTaxCode() + " and hpan " + card.getHpan());
        try {
            ConsentResponse consentResponse = consentClient.getConsent(card.getTaxCode(), card.getHpan(), null);
            return consentResponse.cardHasConsent(card.getHpan());
        } catch (FeignException fe) {
            if (fe.status() == HttpStatus.NOT_FOUND.value()) {
                log.info("Consent not found for card");
                return false;
            } else {
                log.error(fe);
                throw new CardException(CALL_TO_CONSENT_MANAGER_FAILED);
            }
        } catch (Exception e) {
            log.error(e);
            throw new CardException(CALL_TO_CONSENT_MANAGER_FAILED);
        }
    }

}
