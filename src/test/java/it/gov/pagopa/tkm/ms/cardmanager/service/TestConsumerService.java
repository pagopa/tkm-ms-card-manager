package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.ConsumerServiceImpl;
import org.bouncycastle.openpgp.PGPException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@ExtendWith(MockitoExtension.class)
class TestConsumerService {

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Mock
    private ReaderQueueService readerQueueService;

    @Mock
    private Future<Void> mockFuture;

    @Test
    void givenNewCard_success() throws Exception {
        Mockito.when(readerQueueService.workOnMessage(Mockito.anyString())).thenReturn(mockFuture);
        List<String> messages = Arrays.asList("m1", "m2", "m3");
        consumerService.consume(messages);
        Mockito.verify(readerQueueService, Mockito.times(1)).workOnMessage(messages.get(0));
        Mockito.verify(readerQueueService, Mockito.times(1)).workOnMessage(messages.get(1));
        Mockito.verify(readerQueueService, Mockito.times(1)).workOnMessage(messages.get(2));
        Mockito.verify(mockFuture, Mockito.times(messages.size())).get();
    }

//todo giuseppe
//    @Test
//    void givenNewCard_InvalidMessageFormat() throws Exception {
//        try (MockedStatic<PgpStaticUtils> pgpStaticUtilsMockedStatic = mockStatic(PgpStaticUtils.class)) {
//            String message = "MESSAGE";
//            pgpStaticUtilsMockedStatic.when(() -> PgpStaticUtils.decrypt(anyString(), any(), any())).thenReturn(message);
//            when(mapper.readValue(Mockito.anyString(), Mockito.eq(ReadQueue.class))).thenReturn(new ReadQueue());
//            CardException cardException = Assertions.assertThrows(CardException.class, () -> consumerService.consume(message, "true"));
//            Assertions.assertEquals(MESSAGE_VALIDATION_FAILED, cardException.getErrorCode());
//        }
//    }
//
//    @Test
//    void givenNewCard_InvalidMessageEncryption() {
//        try (MockedStatic<PgpStaticUtils> pgpStaticUtilsMockedStatic = mockStatic(PgpStaticUtils.class)) {
//            String message = "MESSAGE";
//            pgpStaticUtilsMockedStatic.when(() -> PgpStaticUtils.decrypt(anyString(), any(), any())).thenThrow(new PGPException(""));
//            CardException cardException = Assertions.assertThrows(CardException.class, () -> consumerService.consume(message, "true"));
//            Assertions.assertEquals(MESSAGE_DECRYPTION_FAILED, cardException.getErrorCode());
//        }
//    }

}
