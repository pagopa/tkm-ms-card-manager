package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum;
import it.gov.pagopa.tkm.ms.cardmanager.exception.KafkaProcessMessageException;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.CardServiceImpl;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.MessageValidatorServiceImpl;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.ReaderQueueServiceImpl;
import it.gov.pagopa.tkm.service.PgpStaticUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.openpgp.PGPException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.Validation;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestReaderQueueService {
    @InjectMocks
    private ReaderQueueServiceImpl readerQueueService;

    @Spy
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private CardServiceImpl cardService;

    @Spy
    private MessageValidatorService validatorService = new MessageValidatorServiceImpl();

    @BeforeEach()
    void init() throws IOException {
        String privateKey = IOUtils.toString(new ClassPathResource("junit_pgp_private.asc").getInputStream(), StandardCharsets.UTF_8.name());
        ReflectionTestUtils.setField(validatorService, "validator", Validation.buildDefaultValidatorFactory().getValidator());
        ReflectionTestUtils.setField(readerQueueService, "tkmReadTokenParPanPvtPgpKey", privateKey);
        ReflectionTestUtils.setField(readerQueueService, "tkmReadTokenParPanPvtPgpKeyPassphrase", "passphrase");
    }

    @Test
    void givenNewCard_InvalidMessageFormat() throws Exception {
        String message = "-----BEGIN PGP MESSAGE-----\n" +
                "\n" +
                "hQIMA9/2VjHdvNtGAQ/+Pd9l649U+ZGQ7cCcCqxd+9wf4N3tPexGe46DwCgKQN3U\n" +
                "KkAvaeeYz6/K1U0Ximpvg1QGxThg5fRKhKI1+ZSE0M4vX8VFDI9TtmAGl++9dCMx\n" +
                "GKONAEO5gtGpocNur5VCJ6je/VNSh5Gr7Cb2lbKMNeVrVRO4SCYrhhS1MEwmRooo\n" +
                "YgzCmeFP3eo5bN8u9eLUUcJMxbO8I9bc4DQyRvlOkqU/TaSZwd6d8Yy/L+8POWJQ\n" +
                "jI5wVC/0w4If4nOww9E7XydMM8fIhJyUasOoZlVe+lwPIbZjxH49OiWfjH1cbGuu\n" +
                "qVErL273shNfG6wIkPZ9VnV3dJg1MFmqrpPoEn0Oh8TQkR89PyjVW+L6coaRtAmD\n" +
                "aRbfTUoDqlvH4n/c55M6U6X7mFpoC9Lnrm604JytZ9/jgRbycITqA78mzRk/WIvk\n" +
                "OTb/auzmeJgAa5PcjeI+d16eVoNGW+kOfb1yT9UR5Q3E6gw0IMF+MG5NF+p33Tt1\n" +
                "3WtDqAd84Ga6f98H+29i4PJRrU5IadnBK5cqeXVL1CRgjuwviDn0fDLIWIHc3j0D\n" +
                "2TLLlKJpUOEsVD8LUjRYQCEot7v9cT4qRbB6YF+gTTD/kx+1mAMKRRwKOdPmdl28\n" +
                "NdYUM7FumOuPNNtXeFqmTu8h6B9VnthrU007xxF0DhMHXG391smjDqUW424fZF3S\n" +
                "VgGAttgTMedDAlFY2EdYuvq2p8H6Qco3ehUEIvR7xGc5qSpQKF6LT3sOzxB6Zw+o\n" +
                "A5MXJ+gg+uAk3vt/p12O9eRGagRZEIgWQCfQKfm7HWK4DHBJbpaT\n" +
                "=tl+W\n" +
                "-----END PGP MESSAGE-----\n";
        readerQueueService.workOnMessage(message);
        Mockito.verify(cardService, never()).updateOrCreateCard(any(ReadQueue.class));
    }

    @Test
    void givenNewCard_InvalidMessageEncryption() {
        try (MockedStatic<PgpStaticUtils> pgpStaticUtilsMockedStatic = mockStatic(PgpStaticUtils.class)) {
            String message = "MESSAGE";
            pgpStaticUtilsMockedStatic.when(() -> PgpStaticUtils.decrypt(anyString(), any(), any())).thenThrow(new PGPException(""));
            readerQueueService.workOnMessage(message);
            Mockito.verify(cardService, never()).updateOrCreateCard(any(ReadQueue.class));
        }
    }

    @Test
    void givenNewCard_KafkaProcessMessageException() {
        String message = "-----BEGIN PGP MESSAGE-----\n" +
                "\n" +
                "hQIMA9/2VjHdvNtGAQ//bXT1b8Wz0EKGLBF4nE9tb5sRmSPPmimzZ15kNO0blOCP\n" +
                "JZrVQek3KFcHLZEj97RjBloLdQrFDvcNhzZqGONOLZi9eYtcjX0gAW69zz4JG+yy\n" +
                "8TCg7Yw+iVzchY4I4Vg/O0Fx7E3F51e0NCwRRiztteDLrBwbe6HH950B19BK7ocF\n" +
                "Ou7eULblfQntaxGL+zaju+4QGYXOYVKMgaFSmgxYfSbC5TIrbnS156R1pRerSWQG\n" +
                "Lx1cB+tDjwdqkQOAMrJVPnMxlW/6rQA7BZ7j5JYEln4ME43yOX9HEqNoImBWN8p3\n" +
                "rCaxaAOO8BjzXuh637Zb8DxMflJCTYvtUesRE6yeUr94Lq0tmy580ytRJfCKDHFL\n" +
                "SzDlxtpPQ9LRZmM+DULACMbhiNF7Yu9HTEp2ULbiYA1asJ1Bn9LzQhXWNCziFpBe\n" +
                "ReKPusl72qTXwlQBdcA2nceuxqilDShAWIm2OzisKHzAvrQR/NJpfwlPd0bQ8V32\n" +
                "N02mqL/8FnESRuIaP79Z+ISKpoW62yonL7GEwj7e/bA6rO/8SS4I8QdbqZXGjmHx\n" +
                "wtgCHJZCFJzWFzQTOGa++VTdifvJDotwF/Qz7WN+uR/TOCwpvP73UbiLcB77Lc3i\n" +
                "I+XIjaNKZ05gYpq0OSMLndGLhiuvJClxkV/CkVTbFcQsI56UQ8QP6khY7btyERzS\n" +
                "aQGsRX0kBQ7FrZebmWp/HgakSVARl/4AXuS51xZVYmg/KPYGYNVJ6wHHzJRzhAOz\n" +
                "K3F5Ule4piNk+M6DS5PDha1pigoKBA2RJW7Ic1RlVKzFRT8154xMmKtVt80Qmrcr\n" +
                "7nXGJLKp60uvsg==\n" +
                "=5nhg\n" +
                "-----END PGP MESSAGE-----\n";
        doThrow(new KafkaProcessMessageException(ErrorCodeEnum.CALL_TO_RTD_FAILED)).when(cardService).updateOrCreateCard(any(ReadQueue.class));
        KafkaProcessMessageException exception = Assertions.assertThrows(KafkaProcessMessageException.class,
                () -> readerQueueService.workOnMessage(message));
        Assertions.assertEquals(message, exception.getMsg());
        ReadQueue readQueue = ReadQueue.builder().pan("123456788900000").circuit(CircuitEnum.VISA).build();
        Mockito.verify(cardService, times(1)).updateOrCreateCard(readQueue);
    }

    //todo add success case
}