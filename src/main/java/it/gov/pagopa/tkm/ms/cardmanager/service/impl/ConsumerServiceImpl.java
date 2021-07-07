package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete.DeleteQueueMessage;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.service.ConsumerService;
import it.gov.pagopa.tkm.service.PgpStaticUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.MESSAGE_DECRYPTION_FAILED;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.MESSAGE_VALIDATION_FAILED;

@Service
@Log4j2
public class ConsumerServiceImpl implements ConsumerService {

    @Autowired
    private DeleteCardServiceImpl deleteCardService;

    @Autowired
    private CardServiceImpl cardService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Validator validator;

    @Value("${keyvault.tkmReadTokenParPanPvtPgpKey}")
    private String tkmReadTokenParPanPvtPgpKey;

    @Value("${keyvault.tkmReadTokenParPanPvtPgpKeyPassphrase}")
    private String tkmReadTokenParPanPvtPgpKeyPassphrase;

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.read-queue.name}",
            groupId = "${spring.kafka.topics.read-queue.group-id}",
            clientIdPrefix = "${spring.kafka.topics.read-queue.client-id}",
            properties = {"sasl.jaas.config:${keyvault.tkmReadTokenParPanConsumerSaslJaasConfig}"},
            concurrency = "${spring.kafka.topics.read-queue.concurrency}")
    public void consume(
            @Payload String message,
            @Header(value = ApiParams.FROM_ISSUER_HEADER, required = false) String fromIssuer
    ) throws JsonProcessingException {
        log.debug("Reading message from queue: " + message);
        String decryptedMessage;
        try {
            decryptedMessage = PgpStaticUtils.decrypt(message, tkmReadTokenParPanPvtPgpKey, tkmReadTokenParPanPvtPgpKeyPassphrase);
        } catch (Exception e) {
            log.error(e);
            throw new CardException(MESSAGE_DECRYPTION_FAILED);
        }
        log.trace("Decrypted message from queue: " + decryptedMessage);
        ReadQueue readQueue = mapper.readValue(decryptedMessage, ReadQueue.class);
        validateMessage(readQueue);
        cardService.updateOrCreateCard(readQueue, Boolean.parseBoolean(fromIssuer));
    }

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.delete-queue.name}",
            groupId = "${spring.kafka.topics.delete-queue.group-id}",
            clientIdPrefix = "${spring.kafka.topics.delete-queue.client-id}",
            properties = {"sasl.jaas.config:${keyvault.tkmDeleteCardConsumerSaslJaasConfig}"},
            concurrency = "${spring.kafka.topics.delete-queue.concurrency}")
    public void consumeDelete(String message) {
        log.debug("Delete message not parsed " + message);
        try {
            DeleteQueueMessage deleteQueueMessage = mapper.readValue(message, DeleteQueueMessage.class);
            log.debug("Delete message  parsed " + deleteQueueMessage);
            validateMessage(deleteQueueMessage);
            deleteCardService.deleteCard(deleteQueueMessage);
            log.info("Card Deleted: " + deleteQueueMessage.getHpan());
        } catch (CardException | JsonProcessingException e) {
            log.error("Invalid message " + message, e);
        }
    }

    private <T> void validateMessage(T message) {
        Set<ConstraintViolation<T>> violations = validator.validate(message);
        if (!CollectionUtils.isEmpty(violations)) {
            log.error("Validation errors: " + violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; ")));
            throw new CardException(MESSAGE_VALIDATION_FAILED);
        }
    }

}
