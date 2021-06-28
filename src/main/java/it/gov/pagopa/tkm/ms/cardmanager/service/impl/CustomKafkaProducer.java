package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class CustomKafkaProducer {

    private KafkaProducer<String, String> producer;
    private String topic;

    public CustomKafkaProducer(String topic, int instanceNumber) {
        this.topic = topic;
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("client.id", "QuotesDownloader" + instanceNumber);
        properties.put("key.serializer", StringSerializer.class);
        properties.put("value.serializer", StringSerializer.class);
        this.producer = new KafkaProducer<>(properties);
    }

    public boolean send(String key, String value) {
        try {
            this.producer.send(new ProducerRecord<>(this.topic, key, value)).get();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendWithHeader(String key, String value, String header, byte[] headerContent) {
        try {
            ProducerRecord producerRecord = new ProducerRecord<>(this.topic, key, value);
            producerRecord.headers().add(header, headerContent);
            this.producer.send(producerRecord).get();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
