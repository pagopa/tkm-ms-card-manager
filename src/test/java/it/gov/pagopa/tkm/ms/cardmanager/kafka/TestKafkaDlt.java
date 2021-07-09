package it.gov.pagopa.tkm.ms.cardmanager.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.tkm.ms.cardmanager.batch.BatchScheduler;
import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class TestKafkaDlt {


    @InjectMocks
    private BatchScheduler scheduler;

    @Mock
    private KafkaProducer<String, String> producer;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private Consumer<String, String> dltConsumer;

    @Mock
    private KafkaTemplate<String, String> dltReadProducer;

    private DefaultBeans testBeans;


    @BeforeEach()
    void init() {
        testBeans = new DefaultBeans();
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
    }


    @Test
    void givenTopicRecords_sendToOriginalTopic(){

        ReflectionTestUtils.setField(scheduler, "readQueueTopic", "tkm-read-token-par-pan");
        ReflectionTestUtils.setField(scheduler, "dltQueueTopic", "deadLetterTopic");
        ReflectionTestUtils.setField(scheduler, "partitions", Collections.singletonList(testBeans.READ_TOPIC_PARTITION));

        when(dltConsumer.poll(Duration.ofSeconds(5))).thenReturn(testBeans.CONSUMER_RECORDS);
        scheduler.scheduledTask();
        verify(dltReadProducer).send((ProducerRecord<String, String>) Mockito.any());


    }




}
