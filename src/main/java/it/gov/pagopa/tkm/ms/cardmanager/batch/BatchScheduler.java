package it.gov.pagopa.tkm.ms.cardmanager.batch;

import it.gov.pagopa.tkm.ms.cardmanager.config.KafkaConfiguration;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
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
import java.util.List;
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
    private  String readQueueTopic;

    @Value("${spring.kafka.topics.write-queue.name}")
   private String writeQueueTopic;

    @Value("${spring.kafka.topics.delete-queue.name}")
    private String deleteQueueTopic;

    private  List<TopicPartition> partitions;

    @Scheduled(cron = "${batch.kafka-dlt-read.cron}")
    public void scheduledTask() {

        partitions = getTopicPartitions();

        if (partitions==null) return;

        dltConsumer.assign(partitions);
        dltConsumer.resume(partitions);

        ConsumerRecords<String, String> consumerRecords = consumerReceive(partitions);

        if (consumerRecords == null) return;

        consumerRecords.iterator().forEachRemaining(
                record -> {
                    String key = record.key();
                    String recordValue = record.value();
                    Headers headers = record.headers();

                    Header originalTopicHeader = record.headers().lastHeader(KafkaConfiguration.originalTopicHeader);
                    byte[] originalTopicHeaderByte = originalTopicHeader.value();
                    String originalTopicHeaderString = new String(originalTopicHeaderByte, StandardCharsets.UTF_8);

                    Header numberOfAttemptsHeader = record.headers().lastHeader(KafkaConfiguration.attemptsCounterHeader);
                    byte[] numberOfAttemptsHeaderByte = numberOfAttemptsHeader.value();
                    String numberOfAttemptsHeaderString = new String(numberOfAttemptsHeaderByte, StandardCharsets.UTF_8);
                    int headerIntValue = Integer.parseInt(numberOfAttemptsHeaderString);
                    if (headerIntValue < 3) {
                        sendBacktoOriginalQueue(originalTopicHeaderString, key, recordValue, headers, numberOfAttemptsHeaderString);
                    }
                }
        );

    }


    private void sendBacktoOriginalQueue(String topic, String key, String recordValue, Headers headers,  String numberOfAttemptsHeaderString) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, recordValue);
        List<Header> headerList = Arrays.asList(headers.toArray());
        headerList.forEach(h->producerRecord.headers().add(h.key(), h.value()));

        producerRecord.headers().add(KafkaConfiguration.attemptsCounterHeader, numberOfAttemptsHeaderString.getBytes());

        if (topic.equals(readQueueTopic)) {
            dltReadProducer.send(producerRecord);
        } else if (topic.equals(writeQueueTopic)) {
            dltWriteProducer.send(producerRecord);
        } else if (topic.equals(deleteQueueTopic)) {
            dltDeleteProducer.send(producerRecord);
        } else {
            log.info("No corresponding topic found");

        }

    }

    private List<TopicPartition> getTopicPartitions(){
        List<PartitionInfo> partitionInfos = dltConsumer.partitionsFor(dltQueueTopic);

          return partitionInfos==null? null:
                partitionInfos
                .stream()
                .map(partitionInfo ->
                        new TopicPartition(dltQueueTopic, partitionInfo.partition()))
                .collect(Collectors.toList());
    }

    private ConsumerRecords<String, String> consumerReceive(List<TopicPartition> partitions) {
        ConsumerRecords<String, String> records = null;
        try {
            records = dltConsumer.poll(Duration.ofSeconds(5));
            dltConsumer.commitSync();
            dltConsumer.pause(partitions);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return records;
    }


}