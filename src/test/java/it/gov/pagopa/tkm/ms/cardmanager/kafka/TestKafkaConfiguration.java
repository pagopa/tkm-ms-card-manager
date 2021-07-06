package it.gov.pagopa.tkm.ms.cardmanager.kafka;

import it.gov.pagopa.tkm.ms.cardmanager.config.KafkaConfiguration;
import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class TestKafkaConfiguration {

    @InjectMocks
    private KafkaConfiguration kafkaConfiguration;

    private DefaultBeans testBeans;

    @BeforeEach()
    void init() {
        testBeans = new DefaultBeans();
    }

    @Test
     void seekToCurrentErrorHandlerRecovers() {
        @SuppressWarnings("unchecked")
        BiConsumer<ConsumerRecord<?, ?>, Exception> recoverer = kafkaConfiguration.recoverer(mock(KafkaTemplate.class));

        SeekToCurrentErrorHandler eh = new SeekToCurrentErrorHandler(recoverer, new FixedBackOff(0L, 1));
        List<ConsumerRecord<?, ?>> records = new ArrayList<>();
        records.add(new ConsumerRecord<>("tkm-read-token-par-pan", 0, 0, UUID.randomUUID().toString(), "value1"));
        records.add(new ConsumerRecord<>("tkm-read-token-par-pan", 0, 1, UUID.randomUUID().toString(), "value2"));
        Consumer<?, ?> consumer = mock(Consumer.class);
        assertThatExceptionOfType(KafkaException.class).isThrownBy(() ->
                eh.handle(new RuntimeException(), records, consumer, null));
        verify(consumer).seek(new TopicPartition("tkm-read-token-par-pan", 0),  0L);
        verifyNoMoreInteractions(consumer);
      }

}
