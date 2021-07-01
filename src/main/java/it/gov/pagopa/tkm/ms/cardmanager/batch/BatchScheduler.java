package it.gov.pagopa.tkm.ms.cardmanager.batch;

import it.gov.pagopa.tkm.ms.cardmanager.config.KafkaConfiguration;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BatchScheduler {

    @Autowired
    @Qualifier("dltKafkaTemplate")
    private KafkaTemplate<String, String> dltKafkaTemplate;

    @Autowired
    @Qualifier("dltConsumer")
    private Consumer<String, String> dltConsumer;

    @Value("${spring.kafka.topics.dlt-queue}")
    private String dltQueueTopic;

    private List<TopicPartition> partitions;

    @Scheduled(cron = "${batch.kafka-dlt-read.cron}")
    public void scheduledTask() {

        partitions=getTopicPartitions();

        dltConsumer.assign(partitions);
        dltConsumer.resume(partitions);

        ConsumerRecords<String,String> consumerRecords = consumerReceive(partitions);
        if (consumerRecords==null) return;

        consumerRecords.iterator().forEachRemaining(
                record-> {
                    String recordValue = record.value();

                    Header originalTopicHeader = record.headers().lastHeader(KafkaConfiguration.originalTopicHeader);
                    byte[] originalTopicHeaderByte = originalTopicHeader.value();
                    String originalTopicHeaderString = new String(originalTopicHeaderByte, StandardCharsets.UTF_8);

                    Header numberOfAttemptsHeader = record.headers().lastHeader(KafkaConfiguration.attemptsCounterHeader);
                    byte[] numberOfAttemptsHeaderByte = numberOfAttemptsHeader.value();
                    String numberOfAttemptsHeaderString = new String(numberOfAttemptsHeaderByte, StandardCharsets.UTF_8);
                    int headerIntValue = Integer.parseInt(numberOfAttemptsHeaderString);

                    if (headerIntValue<3){

                     ProducerRecord<String,String> producerRecord = new ProducerRecord<>(originalTopicHeaderString, recordValue);
                        producerRecord.headers().add(KafkaConfiguration.attemptsCounterHeader, numberOfAttemptsHeaderString.getBytes());
                        dltKafkaTemplate.send(producerRecord);
                    }

                }
        );

    }

    private List<TopicPartition> getTopicPartitions(){
        return dltConsumer
                .partitionsFor(dltQueueTopic)
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