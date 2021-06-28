package it.gov.pagopa.tkm.ms.cardmanager.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfiguration {

    @Value("${spring.kafka.topics.dlt-queue}")
    private String dltQueueTopic;

    public static final String attemptsCounterHeader="attemptsCounter" ;
    public static final String originalTopicHeader="originalTopic";


    /**
     * Boot will autowire this into the container factory.
     */
   @Bean
    public SeekToCurrentErrorHandler errorHandlerKafka(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
       return new SeekToCurrentErrorHandler(deadLetterPublishingRecoverer);
    }

    /**
     * Configure the {@link DeadLetterPublishingRecoverer} to publish poison pill bytes to a dead letter topic:
     * "stock-quotes-avro.DLT".
     */

    @Bean
    public DeadLetterPublishingRecoverer recoverer(KafkaTemplate<String, String> bytesTemplate) {
        return new DeadLetterPublishingRecoverer(bytesTemplate,
                (record, ex) -> {

                    Header retriesHeader = record.headers().lastHeader(attemptsCounterHeader);
                    String retriesIntValue="1";
                   if (retriesHeader!=null) {

                        byte[] value = retriesHeader.value();
                       String stringValue = new String(value, StandardCharsets.UTF_8);
                       int intValue = Integer.parseInt(stringValue);
                       intValue++;
                       retriesIntValue= Integer.toString(intValue);
                    }

                    record.headers().add(attemptsCounterHeader, retriesIntValue.getBytes());
                    record.headers().add(originalTopicHeader, record.topic().getBytes());

                    return new TopicPartition(dltQueueTopic, -1);
                });

    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

   @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

}
