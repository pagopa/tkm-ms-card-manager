package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.CardServiceImpl;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.ConsumerServiceImpl;
import it.gov.pagopa.tkm.service.PgpStaticUtils;
import org.bouncycastle.openpgp.PGPException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.Validation;
import javax.validation.Validator;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.MESSAGE_DECRYPTION_FAILED;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.MESSAGE_VALIDATION_FAILED;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestConsumerService {

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Mock
    private ObjectMapper mapper;

    @Spy
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private CardServiceImpl cardService;

    @BeforeEach
    void initSpringValue() {
        ReflectionTestUtils.setField(consumerService, "tkmReadTokenParPanPvtPgpKey", "tkmReadTokenParPanPvtPgpKey");
        ReflectionTestUtils.setField(consumerService, "tkmReadTokenParPanPvtPgpKeyPassphrase", "tkmReadTokenParPanPvtPgpKeyPassphrase");
    }

    @Test
    void givenNewCard_InvalidMessageFormat() throws Exception {
        try (MockedStatic<PgpStaticUtils> pgpStaticUtilsMockedStatic = mockStatic(PgpStaticUtils.class)) {
            String message = "MESSAGE";
            pgpStaticUtilsMockedStatic.when(() -> PgpStaticUtils.decrypt(anyString(), any(), any())).thenReturn(message);
            when(mapper.readValue(Mockito.anyString(), Mockito.eq(ReadQueue.class))).thenReturn(new ReadQueue());
            CardException cardException = Assertions.assertThrows(CardException.class, () -> consumerService.consume(message, "true"));
            Assertions.assertEquals(MESSAGE_VALIDATION_FAILED, cardException.getErrorCode());
        }
    }

    @Test
    void givenNewCard_InvalidMessageEncryption() {
        try (MockedStatic<PgpStaticUtils> pgpStaticUtilsMockedStatic = mockStatic(PgpStaticUtils.class)) {
            String message = "MESSAGE";
            pgpStaticUtilsMockedStatic.when(() -> PgpStaticUtils.decrypt(anyString(), any(), any())).thenThrow(new PGPException(""));
            CardException cardException = Assertions.assertThrows(CardException.class, () -> consumerService.consume(message, "true"));
            Assertions.assertEquals(MESSAGE_DECRYPTION_FAILED, cardException.getErrorCode());
        }
    }

}
