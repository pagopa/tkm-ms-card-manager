package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.databind.*;
import it.gov.pagopa.tkm.ms.cardmanager.exception.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import it.gov.pagopa.tkm.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import javax.validation.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestConsumerService {

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Mock
    private PgpUtils pgpUtils;

    @Mock
    private ObjectMapper mapper;

    @Spy
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private CardServiceImpl cardService;

    @Test
    void givenNewCard_InvalidMessageFormat() throws Exception {
        String message = "MESSAGE";
        when(pgpUtils.decrypt(Mockito.anyString())).thenReturn(message);
        when(mapper.readValue(Mockito.anyString(), Mockito.eq(ReadQueue.class))).thenReturn(new ReadQueue());
        CardException cardException = Assertions.assertThrows(CardException.class, () -> consumerService.consume(message, "true"));
        Assertions.assertEquals(MESSAGE_VALIDATION_FAILED, cardException.getErrorCode());
    }

    @Test
    void givenNewCard_InvalidMessageEncryption() throws Exception {
        String message = "MESSAGE";
        when(pgpUtils.decrypt(Mockito.anyString())).thenThrow(new Exception());
        CardException cardException = Assertions.assertThrows(CardException.class, () -> consumerService.consume(message, "true"));
        Assertions.assertEquals(MESSAGE_DECRYPTION_FAILED, cardException.getErrorCode());
    }

}
