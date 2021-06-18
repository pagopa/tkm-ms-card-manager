package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.*;
import feign.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.consentmanager.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import it.gov.pagopa.tkm.ms.cardmanager.exception.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueue;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.CardServiceImpl;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.ProducerServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.CALL_TO_CONSENT_MANAGER_FAILED;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.MESSAGE_VALIDATION_FAILED;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestCardService {

    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ProducerServiceImpl producerService;

    @Mock
    private ConsentClient consentClient;

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
    void givenNewCard_persistNewCard() {
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenPanParAndExistingPanPar_doNothing() {
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenPanParAndExistingPan_updateCard() {
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenPanParAndExistingPar_updateCard() {
        
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenPanAndExistingPan_doNothing() {
        
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_1);
    }

    @Test
    void givenPanAndExistingPanPar_doNothing() {
        
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenParAndExistingPar_doNothing() {
        
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAR_1);
    }

    @Test
    void givenParAndExistingPanPar_doNothing() {
        
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenNewTokensAndExistingCard_replaceTokensIfNew() {
        testBeans.TKM_CARD_TOKEN_2.setDeleted(true);
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2, testBeans.TKM_CARD_TOKEN_3
        ));
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        testBeans.READ_QUEUE_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_1);
        testBeans.TKM_CARD_PAR_1.setTokens(updatedTokens);
        verify(cardRepository).save(testBeans.TKM_CARD_PAR_1);
    }

    @Test
    void givenPanParAndExistingPanAndExistingPar_mergeCards() {
        testBeans.TKM_CARD_TOKEN_2.setDeleted(true);
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2, testBeans.TKM_CARD_TOKEN_3
        ));
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1))
                .thenReturn(testBeans.TKM_CARD_PAN_1);
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        testBeans.READ_QUEUE_PAN_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verify(cardRepository).delete(testBeans.TKM_CARD_PAR_1);
        testBeans.TKM_CARD_PAN_PAR_1.setTokens(updatedTokens);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenPanParAndExistingParAndExistingPan_mergeCards() {
        testBeans.TKM_CARD_TOKEN_2.setDeleted(true);
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2, testBeans.TKM_CARD_TOKEN_3
        ));
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1))
                .thenReturn(null).thenReturn(testBeans.TKM_CARD_PAN_1);
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        testBeans.READ_QUEUE_PAN_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verify(cardRepository).delete(testBeans.TKM_CARD_PAN_1);
        testBeans.TKM_CARD_PAN_PAR_1.setTokens(updatedTokens);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenIncompleteCard_dontWriteOnQueue() throws JsonProcessingException {
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1);
        verify(producerService, never()).sendMessage(Mockito.any(WriteQueue.class));
    }

    @Test
    void givenNewCompleteCard_writeOnQueue() throws JsonProcessingException {
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_NEW_CARD);
    }

    @Test
    void givenUpdatedCard_writeOnQueue() throws JsonProcessingException {
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1).thenReturn(null);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_2);
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_UPDATED_CARD);
    }

    @Test
    void givenNotConsentCard_writeOnQueue() {
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1).thenReturn(null);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Deny));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verifyNoInteractions(producerService);
    }

    @Test
    void givenExceptionOnCallToConsentClient_throwException() {
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenThrow(FeignException.class);
        CardException cardException = Assertions.assertThrows(CardException.class, () -> cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1));
        Assertions.assertEquals(CALL_TO_CONSENT_MANAGER_FAILED, cardException.getErrorCode());
    }

    @Test
    void givenConsentNotFound_dontWriteOnQueue() throws JsonProcessingException {
        FeignException.NotFound notFound = new FeignException.NotFound("", mock(Request.class), null);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenThrow(notFound);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verify(producerService, never()).sendMessage(Mockito.any(WriteQueue.class));
    }

}
