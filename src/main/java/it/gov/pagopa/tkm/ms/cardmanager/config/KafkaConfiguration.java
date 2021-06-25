package it.gov.pagopa.tkm.ms.cardmanager.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.LoggingErrorHandler;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableKafka
public class KafkaConfiguration {

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
//    @Bean
//    public DeadLetterPublishingRecoverer publisher(KafkaTemplate<String, String> bytesTemplate) {
//        return new DeadLetterPublishingRecoverer(bytesTemplate);
//    }

    @Bean
    public DeadLetterPublishingRecoverer recoverer(KafkaTemplate<String, String> bytesTemplate) {
        return new DeadLetterPublishingRecoverer(bytesTemplate,
                (record, ex) -> {
                    record.headers().add("dee", "dd".getBytes());
                    return new TopicPartition("morti", -1);
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

  /*  @Bean
    public LoggingErrorHandler errorHandler() {
        return new LoggingErrorHandler();
    } */


    /*@Bean
    public RetryTopicConfiguration dltRetryTopic(KafkaTemplate<String, String> template) {
        return RetryTopicConfigurationBuilder
                .newInstance()
                .fixedBackOff(2000)
                .maxAttempts(3)
              //  .includeTopics("my-topic", "my-other-topic")
              //  .retryOn(MyException.class)
                .create(template);
    } */

}
