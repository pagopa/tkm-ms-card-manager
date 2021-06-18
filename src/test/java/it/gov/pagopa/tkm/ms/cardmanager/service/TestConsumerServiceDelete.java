package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete.DeleteQueueMessage;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.Validation;
import javax.validation.Validator;
import java.time.Instant;

import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestConsumerServiceDelete {

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Spy
    private ObjectMapper mapper;

    @Mock
    private DeleteCardServiceImpl deleteCardService;

    @Spy
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private DefaultBeans testBeans;

    @BeforeEach()
    void init() {
        testBeans = new DefaultBeans();
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
    }

    @Test
    void consumeDelete_happyFlow() throws JsonProcessingException {
        DeleteQueueMessage build = DeleteQueueMessage.builder()
                .taxCode(testBeans.TAX_CODE_1)
                .hpan(testBeans.HPAN_1)
                .timestamp(Instant.now())
                .build();
        String message = mapper.writeValueAsString(build);
        consumerService.consumeDelete(message);
        verify(deleteCardService).deleteCard(mapper.readValue(message, DeleteQueueMessage.class));
    }

    @Test
    void consumeDelete_InvalidMessage() throws JsonProcessingException {
        DeleteQueueMessage build = DeleteQueueMessage.builder()
                .taxCode(testBeans.TAX_CODE_1)
                .hpan(testBeans.HPAN_1)
                .build();
        String message = mapper.writeValueAsString(build);
        consumerService.consumeDelete(message);
        verify(deleteCardService, never()).deleteCard(Mockito.any(DeleteQueueMessage.class));
    }
}