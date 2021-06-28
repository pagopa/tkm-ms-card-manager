package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class CustomKafkaConsumer {

    private KafkaConsumer<String, String> consumer;
    private String topic;

    public CustomKafkaConsumer(String topic, int instanceNumber) {
        this.topic = topic;
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("group.id", "QuotesDownloader4");
        properties.put("client.id", "QuotesDownloader" + instanceNumber);
        properties.put("auto.commit.enable", "true");
        properties.put("key.deserializer", StringDeserializer.class);
        properties.put("value.deserializer", StringDeserializer.class);
       // properties.put("max.poll.records", 1);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<>(properties);
        this.consumer.subscribe(Arrays.asList(this.topic));
    }

    public ConsumerRecords<String, String> receive() {
        ConsumerRecords<String, String> records = null;
        try {
            System.out.println("Receiving records...");
            records = this.consumer.poll(Duration.ofSeconds(10));
            this.consumer.commitSync();
            this.consumer.close();
            System.out.printf("%s records received\n", records.count());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
    }

    public void close() {
        this.consumer.close();
    }

}
