package it.gov.pagopa.tkm.ms.cardmanager.service;

import it.gov.pagopa.tkm.ms.cardmanager.constant.Constant;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete.DeleteQueueMessage;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.MessageValidatorServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.Validation;
import javax.validation.Validator;
import java.time.Instant;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.MESSAGE_VALIDATION_FAILED;

@ExtendWith(MockitoExtension.class)
class TestMessageValidatorService {
    @InjectMocks
    private MessageValidatorServiceImpl messageValidatorService;
    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validMessage() {
        DeleteQueueMessage buiDeleteQueueMessage = DeleteQueueMessage.builder()
                .taxCode(Constant.TAX_CODE_1)
                .hpan(Constant.HASH_1)
                .timestamp(Instant.now()).build();
        messageValidatorService.validateMessage(buiDeleteQueueMessage);
        Mockito.verify(validator, Mockito.times(1)).validate(buiDeleteQueueMessage);
    }

    @Test
    void invalidMessage() {
        DeleteQueueMessage buiDeleteQueueMessage = DeleteQueueMessage.builder()
                .taxCode("taxCode")
                .hpan("hpan")
                .timestamp(Instant.now()).build();
        CardException cardException = Assertions.assertThrows(CardException.class,
                () -> messageValidatorService.validateMessage(buiDeleteQueueMessage));
        Mockito.verify(validator, Mockito.times(1)).validate(buiDeleteQueueMessage);
        Assertions.assertEquals(MESSAGE_VALIDATION_FAILED, cardException.getErrorCode());
    }
}