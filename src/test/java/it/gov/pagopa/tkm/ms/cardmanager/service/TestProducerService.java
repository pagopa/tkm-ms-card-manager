package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.tkm.ms.cardmanager.constant.Constant;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueue;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.ProducerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestProducerService {

    @InjectMocks
    private ProducerServiceImpl producerService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    private ObjectMapper mapper;

    @BeforeEach
    void init() {
        producerService.init();
    }

    @Test
    void sendMessage_validMessage() throws JsonProcessingException, ParseException {
        ReflectionTestUtils.setField(producerService, "writeQueueTopic", "value");
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Date date = dateFormat.parse("01/01/2000 00:00:00:00000");
        String message = String.format("{\"taxCode\":\"%s\",\"timestamp\":\"%s\",\"cards\":[]}", Constant.TAX_CODE_1, dateFormat.format(date));

        WriteQueue build = WriteQueue.builder()
                .taxCode(Constant.TAX_CODE_1)
                .timestamp(date.toInstant())
                .cards(new HashSet<>())
                .build();
        producerService.sendMessage(build);
        verify(kafkaTemplate).send(Mockito.anyString(), Mockito.eq(message));
    }
}