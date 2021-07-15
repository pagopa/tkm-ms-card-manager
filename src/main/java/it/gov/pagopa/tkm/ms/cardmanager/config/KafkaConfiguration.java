package it.gov.pagopa.tkm.ms.cardmanager.config;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.RecoveringBatchErrorHandler;
import org.springframework.kafka.listener.RetryingBatchErrorHandler;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Configuration
@EnableKafka
public class KafkaConfiguration {

    @Value("${spring.kafka.topics.dlt-queue.name}")
    private String dltQueueTopic;

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String kafkaBootstrapServer;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeSerializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeSerializer;

    @Value("${spring.kafka.topics.read-queue.client-id}")
    private String readProducerClientId;

    @Value("${spring.kafka.topics.write-queue.client-id}")
    private String writeProducerClientId;

    @Value("${spring.kafka.topics.delete-queue.client-id}")
    private String deleteProducerClientId;

    @Value("${spring.kafka.producer.properties.security.protocol}")
    private String producerSecurityProtocol;

    @Value("${spring.kafka.producer.properties.sasl.mechanism}")
    private String producerSaslMechanism;

    @Value("${spring.kafka.consumer.properties.security.protocol}")
    private String consumerSecurityProtocol;

    @Value("${spring.kafka.consumer.properties.sasl.mechanism}")
    private String consumerSaslMechanism;

    @Value("${spring.kafka.topics.read-queue.jaas.config.producer}")
    private String azureSaslJaasConfigRead;

   /* @Value("${spring.kafka.topics.write-queue.jaas.config}")
    private String azureSaslJaasConfigWrite;

    @Value("${spring.kafka.topics.delete-queue.jaas.config}")
    private String azureSaslJaasConfigDelete; */

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeserializer;

    @Value("${spring.kafka.topics.dlt-queue.group-id}")
    private String dltConsumerGroup;

    @Value("${spring.kafka.topics.dlt-queue.client-id}")
    private String dltClientId;

    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private String consumerEnableAutoCommit;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    public static final String attemptsCounterHeader = "attemptsCounter";
    public static final String originalTopicHeader = "originalTopic";

    /**
     * Boot will autowire this into the container factory.
     */
    @Bean
    public SeekToCurrentErrorHandler errorHandlerKafka(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        return new SeekToCurrentErrorHandler(deadLetterPublishingRecoverer);
    }


    @Bean
    public RetryingBatchErrorHandler batchErrorHandler(KafkaTemplate<String, String> template) {

        DeadLetterPublishingRecoverer recoverer = recoverer(template);
             return new RetryingBatchErrorHandler(new FixedBackOff(1000L, 1), recoverer);

    }


    /**
     * Configure the {@link DeadLetterPublishingRecoverer} to publish poison pill bytes to a dead letter topic:
     * "stock-quotes-avro.DLT".
     */


   /*@Bean
    public RecoveringBatchErrorHandler batchErrorHandler(KafkaTemplate<String, String> template) {
        System.out.println("______________RecoveringBatchErrorHandler__________");
        DeadLetterPublishingRecoverer recoverer = recoverer(template);
     //   RecoveringBatchErrorHandler errorHandler =
              return  new RecoveringBatchErrorHandler(recoverer, new FixedBackOff(2L, 5));
    } */

    @Bean
    public DeadLetterPublishingRecoverer recoverer(KafkaTemplate<String, String> bytesTemplate) {

        return new DeadLetterPublishingRecoverer(bytesTemplate,
                (record, ex) -> {

                    Header retriesHeader = record.headers().lastHeader(attemptsCounterHeader);
                    String numberOfAttemptsString = "1";
                    if (retriesHeader != null) {

                        byte[] value = retriesHeader.value();
                        String stringValue = new String(value, StandardCharsets.UTF_8);
                        int numberOfAttemptsInt = Integer.parseInt(stringValue);

                        numberOfAttemptsInt++;
                        numberOfAttemptsString = Integer.toString(numberOfAttemptsInt);
                    }

                    record.headers().add(attemptsCounterHeader, numberOfAttemptsString.getBytes());
                    record.headers().add(originalTopicHeader, record.topic().getBytes());
                    log.info(String.format("Adding record [ %s ] to DeadLetterTopic from original Topic %s - " +
                            "attempt number %s ", record, record.topic(), numberOfAttemptsString));

                    return new TopicPartition(dltQueueTopic, -1);

                });

    }

    @Primary
    @Bean(name = "dltReadProducer")
    public KafkaTemplate<String, String> dltReadProducer() {
        return new KafkaTemplate<>(readProducerFactory());
    }

    @Bean(name = "dltWriteProducer")
    public KafkaTemplate<String, String> dltWriteProducer() {
        return new KafkaTemplate<>(writeProducerFactory());
    }

    @Bean(name = "dltDeleteProducer")
    public KafkaTemplate<String, String> dltDeleteProducer() {
        return new KafkaTemplate<>(deleteProducerFactory());
    }

    @Bean(name = "dltConsumer")
    public Consumer<String, String> dltConsumer() {
        return consumerFactory().createConsumer();
    }


    @Bean
    public ProducerFactory<String, String> readProducerFactory() {
        Map<String, Object> configProps = createConfigProps(false);
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, readProducerClientId);
        configProps.put(SaslConfigs.SASL_JAAS_CONFIG, azureSaslJaasConfigRead);

        return new DefaultKafkaProducerFactory<>(configProps);
    }


    @Bean
    public ProducerFactory<String, String> writeProducerFactory() {
        Map<String, Object> configProps = createConfigProps(false);
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, writeProducerClientId);
        //  configProps.put(SaslConfigs.SASL_JAAS_CONFIG, azureSaslJaasConfigWrite);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, String> deleteProducerFactory() {
        Map<String, Object> configProps = createConfigProps(false);
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, deleteProducerClientId);
        //  configProps.put(SaslConfigs.SASL_JAAS_CONFIG, azureSaslJaasConfigDelete);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = createConfigProps(true);
        configProps.put(ConsumerConfig.CLIENT_ID_CONFIG, dltClientId);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, dltConsumerGroup);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.valueOf(consumerEnableAutoCommit));
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    private Map<String, Object> createConfigProps(boolean consumer) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaBootstrapServer);
        if (consumer) {
            configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
            configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        } else {
            configProps.put(
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    keySerializer);
            configProps.put(
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    valueSerializer);
        }
        configProps.put(StreamsConfig.SECURITY_PROTOCOL_CONFIG,
                consumer ? consumerSecurityProtocol : producerSecurityProtocol);
        configProps.put(SaslConfigs.SASL_MECHANISM,
                consumer ? consumerSaslMechanism : producerSaslMechanism);

        return configProps;
    }



}
