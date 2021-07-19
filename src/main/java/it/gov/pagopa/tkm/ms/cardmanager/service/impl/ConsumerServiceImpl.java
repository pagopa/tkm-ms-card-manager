package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete.DeleteQueueMessage;
import it.gov.pagopa.tkm.ms.cardmanager.service.ConsumerService;
import it.gov.pagopa.tkm.ms.cardmanager.service.MessageValidatorService;
import it.gov.pagopa.tkm.ms.cardmanager.service.ReaderQueueService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
@Log4j2
public class ConsumerServiceImpl implements ConsumerService {

    @Autowired
    private DeleteCardServiceImpl deleteCardService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MessageValidatorService validatorService;

    @Autowired
    private ReaderQueueService readerQueueService;

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.read-queue.name}",
            groupId = "${spring.kafka.topics.read-queue.group-id}",
            clientIdPrefix = "${spring.kafka.topics.read-queue.client-id}",
            properties = {"sasl.jaas.config:${keyvault.tkmReadTokenParPanConsumerSaslJaasConfig}"},
            concurrency = "${spring.kafka.topics.read-queue.concurrency}")
    public void consume(@Payload List<String> messages) throws ExecutionException, InterruptedException, JsonProcessingException {
        List<Future<Void>> futures = new ArrayList<>();
        log.info(String.format("Reading and processing %s messages", CollectionUtils.size(messages)));
        for (String message : messages) {
            futures.add((readerQueueService.workOnMessage(message)));
        }
        for (Future<Void> future : futures) {
            future.get();
        }
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
            validatorService.validateMessage(deleteQueueMessage);
            deleteCardService.deleteCard(deleteQueueMessage);
            log.info("Card Deleted: " + deleteQueueMessage.getHpan());
        } catch (CardException | JsonProcessingException e) {
            log.error("Invalid message " + message, e);
        }
    }


}
