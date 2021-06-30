package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import it.gov.pagopa.tkm.ms.cardmanager.exception.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.*;
import it.gov.pagopa.tkm.service.*;
import lombok.extern.log4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.kafka.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import javax.validation.*;

import java.util.*;
import java.util.stream.*;

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

    @Value("${keyvault.readQueuePrvPgpKey}")
    private byte[] pgpPrivateKey;

    @Value("${keyvault.readQueuePrvPgpKeyPassphrase:null}")
    private char[] pgpPassphrase;

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.read-queue.name}",
            groupId = "${spring.kafka.topics.read-queue.group-id}",
            clientIdPrefix = "${spring.kafka.topics.read-queue.client-id}",
            properties = {"sasl.jaas.config:${keyvault.tkmReadTokenParPanConsumerSaslJaasConfig}"},
            concurrency = "${spring.kafka.topics.read-queue.concurrency}")
    public void consume(String message) throws JsonProcessingException {
        log.debug("Reading message from queue: " + message);
        String decryptedMessage;
        try {
            decryptedMessage = PgpStaticUtils.decryptMessage(message, pgpPrivateKey, pgpPassphrase);
        } catch (Exception e) {
            log.error(e);
            throw new CardException(MESSAGE_DECRYPTION_FAILED);
        }
        log.trace("Decrypted message from queue: " + decryptedMessage);
        ReadQueue readQueue = mapper.readValue(decryptedMessage, ReadQueue.class);
        validateMessage(readQueue);
        cardService.updateOrCreateCard(readQueue);
    }

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.delete-queue.name}",
            groupId = "${spring.kafka.topics.delete-queue.group-id}",
            clientIdPrefix = "${spring.kafka.topics.delete-queue.client-id}",
            properties = {"sasl.jaas.config:${keyvault.cardMEventhubDeleteSaslJaasConfig}"},
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
