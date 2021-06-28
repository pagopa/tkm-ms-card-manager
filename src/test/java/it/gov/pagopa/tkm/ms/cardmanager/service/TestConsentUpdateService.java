package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentEntityEnum;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentResponse;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueue;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.ConsentUpdateServiceImpl;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.ProducerServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.MESSAGE_WRITE_FAILED;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestConsentUpdateService {

    @InjectMocks
    private ConsentUpdateServiceImpl consentUpdateService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ProducerServiceImpl producerService;

    private DefaultBeans testBeans;

    private final MockedStatic<Instant> instantMockedStatic = mockStatic(Instant.class);

    @BeforeEach
    void init() {
        testBeans = new DefaultBeans();
        instantMockedStatic.when(Instant::now).thenReturn(DefaultBeans.INSTANT);
    }

    @AfterAll
    void close() {
        instantMockedStatic.close();
    }

    @Test
    void givenConsentUpdateForCardNotPresentInCardManager_doNothing() throws JsonProcessingException {
        consentUpdateService.updateConsent(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        verify(producerService, never()).sendMessage(Mockito.any(WriteQueue.class));
    }

    //todo
//    @Test
//    void givenGlobalConsentAllowUpdate_writeOnQueue() throws JsonProcessingException {
//        when(cardRepository.findByTaxCodeAndParIsNotNullAndDeletedFalse(testBeans.TAX_CODE_1)).thenReturn(Collections.singletonList(testBeans.TKM_CARD_PAN_PAR_1));
//        consentUpdateService.updateConsent(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
//        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_NEW_CARD);
//    }
//
//    @Test
//    void givenGlobalConsentAllowUpdate_sendError() throws JsonProcessingException {
//        when(cardRepository.findByTaxCodeAndParIsNotNullAndDeletedFalse(testBeans.TAX_CODE_1)).thenReturn(Collections.singletonList(testBeans.TKM_CARD_PAN_PAR_1));
//        Mockito.doThrow(new JsonProcessingException("Error"){}).when(producerService).sendMessage(Mockito.any());
//        ConsentResponse consentUpdateGlobal = testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow);
//        CardException cardException = Assertions.assertThrows(CardException.class, () -> consentUpdateService.updateConsent(consentUpdateGlobal));
//        Assertions.assertEquals(MESSAGE_WRITE_FAILED, cardException.getErrorCode());
//    }
//
//    @Test
//    void givenGlobalConsentDenyUpdate_writeOnQueue() throws JsonProcessingException {
//        when(cardRepository.findByTaxCodeAndParIsNotNullAndDeletedFalse(testBeans.TAX_CODE_1)).thenReturn(Collections.singletonList(testBeans.TKM_CARD_PAN_PAR_1));
//        consentUpdateService.updateConsent(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Deny));
//        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_REVOKED_CONSENT_CARD);
//    }

    @Test
    void givenPartialConsentUpdate_writeOnQueue() throws JsonProcessingException {
//        ConsentResponse consentUpdate = testBeans.getConsentUpdatePartial();
//        when(cardRepository.findByTaxCodeAndHpanInAndParIsNotNullAndDeletedFalse(testBeans.TAX_CODE_1, consentUpdate.retrieveHpans())).thenReturn(Collections.singletonList(testBeans.TKM_CARD_PAN_PAR_1));
//        consentUpdateService.updateConsent(consentUpdate);
//        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_NEW_CARD); todo
    }

}
