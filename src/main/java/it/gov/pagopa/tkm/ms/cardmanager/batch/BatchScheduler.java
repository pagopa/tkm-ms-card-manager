package it.gov.pagopa.tkm.ms.cardmanager.batch;

import it.gov.pagopa.tkm.annotation.LoggingTableResult;
import it.gov.pagopa.tkm.ms.cardmanager.config.KafkaConfiguration;
import it.gov.pagopa.tkm.ms.cardmanager.model.batch.DltBatchResult;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

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

    @SuppressWarnings("UnusedReturnValue")
    @Scheduled(cron = "${batch.kafka-dlt-read.cron}")
    @LoggingTableResult(batchName = "DLT_BATCH")
    public DltBatchResult scheduledTask() {
        DltBatchResult dltBatchResult = new DltBatchResult();
        dltBatchResult.setSuccess(true);
        try {
            dltConsumer.subscribe(Collections.singletonList(dltQueueTopic));

            ConsumerRecords<String, String> consumerRecords = consumerReceive();

            if (consumerRecords != null && !consumerRecords.isEmpty()) {
                List<String> location = new ArrayList<>();
                consumerRecords.iterator().forEachRemaining(queueElement -> location.add(processRecoverRecord(queueElement)));
                addRecordsDetails(location, dltBatchResult);
                dltBatchResult.setNumRecordProcessed(consumerRecords.count());
            }
        } catch (Exception e) {
            log.error("Error on dlt batch recover", e);
            dltBatchResult.setSuccess(false);
            dltBatchResult.setErrorMessage(e.getMessage());
        }
        dltConsumer.unsubscribe();
        return dltBatchResult;
    }

    private void addRecordsDetails(List<String> location, DltBatchResult dltBatchResult) {
        Map<Integer, List<String>> collect = location.stream().collect(Collectors.groupingBy(String::hashCode));
        Map<String, Integer> recordsDetails = new HashMap<>();
        for (Map.Entry<Integer, List<String>> integerListEntry : collect.entrySet()) {
            List<String> value = integerListEntry.getValue();
            recordsDetails.put(value.get(0), value.size());
        }
        dltBatchResult.setRecordsDetails(recordsDetails);
    }

    private String processRecoverRecord(ConsumerRecord<String, String> queueElement) {
        String key = queueElement.key();
        String recordValue = queueElement.value();
        Headers headers = queueElement.headers();

        String originalTopicHeaderString = getValueStringFromRecordHeader(queueElement, KafkaConfiguration.ORIGINAL_TOPIC_HEADER);

        String numberOfAttemptsHeaderString = getValueStringFromRecordHeader(queueElement, KafkaConfiguration.ATTEMPT_COUNTER_HEADER);
        int headerIntValue = Integer.parseInt(numberOfAttemptsHeaderString);
        if (headerIntValue <= 3) {
            sendBackToOriginalQueue(originalTopicHeaderString, key, recordValue, headers, numberOfAttemptsHeaderString);
        }
        return originalTopicHeaderString;
    }

    @NotNull
    private String getValueStringFromRecordHeader(ConsumerRecord<String, String> queueElement, String headerName) {
        Header originalTopicHeader = queueElement.headers().lastHeader(headerName);
        byte[] originalTopicHeaderByte = originalTopicHeader.value();
        return new String(originalTopicHeaderByte, StandardCharsets.UTF_8);
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
        records = dltConsumer.poll(Duration.ofSeconds(1));
        dltConsumer.commitSync();
        log.info("Recover number of records: " + (records != null ? records.count() : 0));
        log.trace("Records: " + records);
        return records;
    }
}