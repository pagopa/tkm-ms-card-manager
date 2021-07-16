package it.gov.pagopa.tkm.ms.cardmanager.batch;

import it.gov.pagopa.tkm.annotation.EnableExecutionTime;
import it.gov.pagopa.tkm.annotation.EnableLoggingTableResult;
import it.gov.pagopa.tkm.annotation.LoggingTableResult;
import it.gov.pagopa.tkm.model.BaseResultDetails;
import it.gov.pagopa.tkm.ms.cardmanager.config.KafkaConfiguration;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Log4j2
@Component
public class BatchScheduler {

    @Autowired
    @Qualifier("dltReadProducer")
    private KafkaTemplate<String, String> dltReadProducer;

    @Autowired
    @Qualifier("dltWriteProducer")
    private KafkaTemplate<String, String> dltWriteProducer;

    @Autowired
    @Qualifier("dltDeleteProducer")
    private KafkaTemplate<String, String> dltDeleteProducer;

    @Autowired
    @Qualifier("dltConsumer")
    private Consumer<String, String> dltConsumer;

    @Value("${spring.kafka.topics.dlt-queue.name}")
    private String dltQueueTopic;

    @Value("${spring.kafka.topics.read-queue.name}")
    private String readQueueTopic;

    @Value("${spring.kafka.topics.write-queue.name}")
    private String writeQueueTopic;

    @Value("${spring.kafka.topics.delete-queue.name}")
    private String deleteQueueTopic;

    @Scheduled(cron = "${batch.kafka-dlt-read.cron}")
    @LoggingTableResult(resultClass = BaseResultDetails.class)
    @EnableExecutionTime
    public void scheduledTask() {
        try {
            dltConsumer.subscribe(Collections.singletonList(dltQueueTopic));

            ConsumerRecords<String, String> consumerRecords = consumerReceive();

            if (consumerRecords == null || consumerRecords.isEmpty()) return;

            consumerRecords.iterator().forEachRemaining(this::processRecoverRecord);
        } catch (Exception e) {
            log.error("Error on dlt batch recover", e);
        }
        dltConsumer.unsubscribe();
    }

    private void processRecoverRecord(ConsumerRecord<String, String> queueElement) {
        String key = queueElement.key();
        String recordValue = queueElement.value();
        Headers headers = queueElement.headers();

        Header originalTopicHeader = queueElement.headers().lastHeader(KafkaConfiguration.ORIGINAL_TOPIC_HEADER);
        byte[] originalTopicHeaderByte = originalTopicHeader.value();
        String originalTopicHeaderString = new String(originalTopicHeaderByte, StandardCharsets.UTF_8);

        Header numberOfAttemptsHeader = queueElement.headers().lastHeader(KafkaConfiguration.ATTEMPT_COUNTER_HEADER);
        byte[] numberOfAttemptsHeaderByte = numberOfAttemptsHeader.value();
        String numberOfAttemptsHeaderString = new String(numberOfAttemptsHeaderByte, StandardCharsets.UTF_8);
        int headerIntValue = Integer.parseInt(numberOfAttemptsHeaderString);
        if (headerIntValue <= 3) {
            sendBackToOriginalQueue(originalTopicHeaderString, key, recordValue, headers, numberOfAttemptsHeaderString);
        }
    }

    private void sendBackToOriginalQueue(String topic, String key, String recordValue, Headers headers, String numberOfAttemptsHeaderString) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, recordValue);
        List<Header> headerList = Arrays.asList(headers.toArray());
        headerList.forEach(h -> producerRecord.headers().add(h.key(), h.value()));

        producerRecord.headers().add(KafkaConfiguration.ATTEMPT_COUNTER_HEADER, numberOfAttemptsHeaderString.getBytes());

        if (topic.equals(readQueueTopic)) {
            dltReadProducer.send(producerRecord);
        } else if (topic.equals(writeQueueTopic)) {
            dltWriteProducer.send(producerRecord);
        } else if (topic.equals(deleteQueueTopic)) {
            dltDeleteProducer.send(producerRecord);
        } else {
            log.error("No corresponding topic found");
        }
    }

    private ConsumerRecords<String, String> consumerReceive() {
        ConsumerRecords<String, String> records;
        records = dltConsumer.poll(Duration.ofSeconds(5));
        dltConsumer.commitSync();
        log.info("Recover number of records: " + (records != null ? records.count() : 0));
        log.trace("Records: " + records);
        return records;
    }
}