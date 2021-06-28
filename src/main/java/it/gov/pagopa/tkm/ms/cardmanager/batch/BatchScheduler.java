package it.gov.pagopa.tkm.ms.cardmanager.batch;

import it.gov.pagopa.tkm.ms.cardmanager.config.KafkaConfiguration;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.CustomKafkaConsumer;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.CustomKafkaProducer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class BatchScheduler {

    @Value("${spring.kafka.topics.dlt-queue}")
    private String dltQueueTopic;

    @Scheduled(cron = "${batch.kafka-dlt-read.cron}")
    public void scheduledTask() {

        CustomKafkaConsumer customKafkaConsumer = new CustomKafkaConsumer(dltQueueTopic, 1);
        ConsumerRecords<String,String> consumerRecords = customKafkaConsumer.receive();
        consumerRecords.iterator().forEachRemaining(
                record-> {
                    String key = record.key();
                    String recordValue = record.value();

                    Header originalTopicHeader = record.headers().lastHeader(KafkaConfiguration.originalTopicHeader);
                    byte[] originalTopicHeaderByte = originalTopicHeader.value();
                    String originalTopicHeaderString = new String(originalTopicHeaderByte, StandardCharsets.UTF_8);

                    Header numberOfAttemptsHeader = record.headers().lastHeader(KafkaConfiguration.attemptsCounterHeader);
                    byte[] numberOfAttemptsHeaderByte = numberOfAttemptsHeader.value();
                    String numberOfAttemptsHeaderString = new String(numberOfAttemptsHeaderByte, StandardCharsets.UTF_8);
                    int headerIntValue = Integer.parseInt(numberOfAttemptsHeaderString);

                    if (headerIntValue<3){
                        CustomKafkaProducer producer = new CustomKafkaProducer( originalTopicHeaderString, 1);
                        producer.sendWithHeader(key, recordValue, KafkaConfiguration.attemptsCounterHeader, numberOfAttemptsHeaderString.getBytes());
                    }

                }

        );

    }
}