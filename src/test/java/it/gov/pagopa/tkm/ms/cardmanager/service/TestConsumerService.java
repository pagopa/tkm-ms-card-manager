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
import org.springframework.test.util.*;

import javax.validation.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestConsumerService {

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Mock
    private ObjectMapper mapper;

    @Spy
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private final MockedStatic<PgpStaticUtils> pgpStaticUtilsMockedStatic = mockStatic(PgpStaticUtils.class);

    @BeforeEach
    void init() {
        pgpStaticUtilsMockedStatic.when(()-> PgpStaticUtils.decrypt(anyString(), anyString(), anyString())).thenReturn("MESSAGE");
        ReflectionTestUtils.setField(consumerService, "pgpPrivateKey", "TEST_PRIVATE_KEY");
        ReflectionTestUtils.setField(consumerService, "pgpPassphrase", "TEST_PASSPHRASE");
    }

    @AfterEach
    void close() {
        pgpStaticUtilsMockedStatic.close();
    }

    @Test
    void givenNewCard_invalidMessageFormat() throws Exception {
        String message = "MESSAGE";
        when(mapper.readValue(anyString(), eq(ReadQueue.class))).thenReturn(new ReadQueue());
        CardException cardException = Assertions.assertThrows(CardException.class, () -> consumerService.consume(message));
        Assertions.assertEquals(MESSAGE_VALIDATION_FAILED, cardException.getErrorCode());
    }

    @Test
    void givenNewCard_InvalidMessageEncryption() throws Exception {
        String message = "MESSAGE";
        when(PgpStaticUtils.decrypt(anyString(), anyString(), anyString())).thenThrow(new RuntimeException());
        CardException cardException = Assertions.assertThrows(CardException.class, () -> consumerService.consume(message));
        Assertions.assertEquals(MESSAGE_DECRYPTION_FAILED, cardException.getErrorCode());
    }

}
