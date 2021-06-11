package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import java.time.*;
import java.util.*;

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

    @Test
    void givenGlobalConsentAllowUpdate_writeOnQueue() throws JsonProcessingException {
        when(cardRepository.findByTaxCodeAndParIsNotNullAndDeletedFalse(testBeans.TAX_CODE_1)).thenReturn(Collections.singletonList(testBeans.TKM_CARD_PAN_PAR_1));
        consentUpdateService.updateConsent(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_NEW_CARD);
    }

    @Test
    void givenGlobalConsentDenyUpdate_writeOnQueue() throws JsonProcessingException {
        when(cardRepository.findByTaxCodeAndParIsNotNullAndDeletedFalse(testBeans.TAX_CODE_1)).thenReturn(Collections.singletonList(testBeans.TKM_CARD_PAN_PAR_1));
        consentUpdateService.updateConsent(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Deny));
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_REVOKED_CONSENT_CARD);
    }

    @Test
    void givenPartialConsentUpdate_writeOnQueue() throws JsonProcessingException {
        ConsentResponse consentUpdate = testBeans.getConsentUpdatePartial();
        when(cardRepository.findByTaxCodeAndHpanInAndParIsNotNullAndDeletedFalse(testBeans.TAX_CODE_1, consentUpdate.getHpans())).thenReturn(Collections.singletonList(testBeans.TKM_CARD_PAN_PAR_1));
        consentUpdateService.updateConsent(consentUpdate);
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_NEW_CARD);
    }

}
