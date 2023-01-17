package it.gov.pagopa.tkm.ms.cardmanager.config;

import it.gov.pagopa.tkm.ms.cardmanager.exception.KafkaProcessMessageException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
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

    @Value("${spring.kafka.producer.bootstrap-servers-cstar}")
    private String kafkaBootstrapServerCstar;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;

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
    private String readQueueJaasConfigProducer;

    @Value("${spring.kafka.topics.write-queue.jaas.config.producer}")
    private String writeQueueJaasConfigProducer;

    @Value("${spring.kafka.topics.delete-queue.jaas.config.producer}")
    private String deleteQueueJaasConfigProducer;

    @Value("${spring.kafka.topics.dlt-queue.jaas.config.consumer}")
    private String dltQueueJaasConfigConsumer;

    @Value("${spring.kafka.topics.dlt-queue.jaas.config.producer}")
    private String dltQueueJaasConfigProducer;

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

    @Value("${spring.kafka.topics.dlt-queue.max-poll-records}")
    private String maxPollRecordsDl;

    public static final String ATTEMPT_COUNTER_HEADER = "attemptsCounter";
    public static final String ORIGINAL_TOPIC_HEADER = "originalTopic";

    @Bean
    public SeekToCurrentErrorHandler errorHandlerKafka(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        return new SeekToCurrentErrorHandler(deadLetterPublishingRecoverer);
    }

    @Bean
    public RetryingBatchErrorHandler batchErrorHandler(@Qualifier("dltProducer") KafkaTemplate<String, String> dltProducer) {
        DeadLetterPublishingRecoverer recover = recoverer(dltProducer);
        return new RetryingBatchErrorHandler(new FixedBackOff(1000L, 1), recover);
    }

    @Bean
    public DeadLetterPublishingRecoverer recoverer(@Qualifier("dltProducer") KafkaTemplate<String, String> dltProducer) {
        return new DeadLetterPublishingRecoverer(dltProducer,
                (queueElement, ex) -> {

                    Header retriesHeader = queueElement.headers().lastHeader(ATTEMPT_COUNTER_HEADER);
                    String numberOfAttemptsString = "1";
                    if (retriesHeader != null) {

                        byte[] value = retriesHeader.value();
                        String stringValue = new String(value, StandardCharsets.UTF_8);
                        int numberOfAttemptsInt = Integer.parseInt(stringValue);

                        numberOfAttemptsInt++;
                        numberOfAttemptsString = Integer.toString(numberOfAttemptsInt);
                    }

                    queueElement.headers().add(ATTEMPT_COUNTER_HEADER, numberOfAttemptsString.getBytes());
                    queueElement.headers().add(ORIGINAL_TOPIC_HEADER, queueElement.topic().getBytes());
                    log.info(String.format("Adding record to DeadLetterTopic from original Topic %s - " +
                            "attempt number %s ", queueElement.topic(), numberOfAttemptsString));

                    Throwable cause = ex.getCause().getCause();
                    if (cause instanceof KafkaProcessMessageException && !isTheSameOfException(cause, queueElement.value()))
                        return null;

                    return new TopicPartition(dltQueueTopic, -1);

                });
    }

    private boolean isTheSameOfException(Throwable ex, Object msg) {
        KafkaProcessMessageException ex1 = (KafkaProcessMessageException) ex;
        return msg instanceof String && StringUtils.equals(ex1.getMsg(), (String) msg);
    }

    @Primary
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return null;
    }

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
        return consumerFactoryDlt().createConsumer();
    }

    @Bean(name = "dltProducer")
    public KafkaTemplate<String, String> dltProducer() {
        return new KafkaTemplate<>(dltProducerFactory());
    }

    private ProducerFactory<String, String> dltProducerFactory() {
        Map<String, Object> configProps = createConfigProps(false, dltClientId, dltQueueJaasConfigProducer);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    private ProducerFactory<String, String> readProducerFactory() {
        Map<String, Object> configProps = createConfigProps(false, readProducerClientId, readQueueJaasConfigProducer);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    private ProducerFactory<String, String> writeProducerFactory() {
        Map<String, Object> configProps = createConfigProps(false, writeProducerClientId, writeQueueJaasConfigProducer, kafkaBootstrapServerCstar);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    private ProducerFactory<String, String> deleteProducerFactory() {
        Map<String, Object> configProps = createConfigProps(false, deleteProducerClientId, deleteQueueJaasConfigProducer);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    private ConsumerFactory<String, String> consumerFactoryDlt() {
        Map<String, Object> configProps = createConfigProps(true, dltClientId, dltQueueJaasConfigConsumer);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, dltConsumerGroup);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.valueOf(consumerEnableAutoCommit));
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecordsDl);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    private Map<String, Object> createConfigProps(boolean consumer, String clientId, String jaas) {
        return createConfigProps(consumer, clientId, jaas, kafkaBootstrapServer);
    }

    private Map<String, Object> createConfigProps(boolean consumer, String clientId, String jaas, String kafkaBootstrapServerParam) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaBootstrapServerParam);
        if (consumer) {
            configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
            configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        } else {
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        }
        configProps.put(StreamsConfig.SECURITY_PROTOCOL_CONFIG,
                consumer ? consumerSecurityProtocol : producerSecurityProtocol);
        configProps.put(SaslConfigs.SASL_MECHANISM,
                consumer ? consumerSaslMechanism : producerSaslMechanism);
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        if (StringUtils.isNotBlank(jaas)) {
            configProps.put(SaslConfigs.SASL_JAAS_CONFIG, jaas);
        }
        return configProps;
    }
}
