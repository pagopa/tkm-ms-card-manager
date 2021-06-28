package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.*;
import feign.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import it.gov.pagopa.tkm.ms.cardmanager.exception.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueue;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.CALL_TO_CONSENT_MANAGER_FAILED;
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

    @Mock
    private CryptoServiceImpl cryptoService;

    @Mock
    private RtdHashingClient rtdHashingClient;

    private DefaultBeans testBeans;

    private final MockedStatic<Instant> instantMockedStatic = mockStatic(Instant.class);

    @BeforeEach
    void init() {
        testBeans = new DefaultBeans();
        instantMockedStatic.when(Instant::now).thenReturn(DefaultBeans.INSTANT);
        ReflectionTestUtils.setField(cardService, "apimRtdSubscriptionKey", "key");
    }

    @AfterAll
    void close() {
        instantMockedStatic.close();
    }

    @Test
    void givenPan_returnHash() {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(rtdHashingClient.getHash(new WalletsHashingEvaluationInput(testBeans.PAN_1), "key")).thenReturn(new WalletsHashingEvaluation(testBeans.HPAN_1, "salt"));
        testBeans.READ_QUEUE_PAN_1.setHpan(null);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1, true);
        verify(rtdHashingClient).getHash(new WalletsHashingEvaluationInput(testBeans.PAN_1), "key");
    }

    @Test
    void givenNewCard_persistNewCard() {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenPanParAndExistingPanPar_doNothing() {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenPanParAndExistingPan_updateCard() {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenPanParAndExistingPar_updateCard() {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenPanAndExistingPan_doNothing() {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_1));
    }

    @Test
    void givenPanAndExistingPanPar_doNothing() {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenParAndExistingPar_doNothing() {
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAR_1));
    }

    @Test
    void givenParAndExistingPanPar_doNothing() {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenNewTokensAndExistingCard_replaceTokensIfNew() {
        testBeans.TKM_CARD_TOKEN_2.setDeleted(true);
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2, testBeans.TKM_CARD_TOKEN_3
        ));
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        testBeans.READ_QUEUE_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_1, true);
        testBeans.TKM_CARD_PAR_1.setTokens(updatedTokens);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAR_1));
    }

    @Test
    void givenPanParAndExistingPanAndExistingPar_mergeCards() {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_3)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_3));
        testBeans.TKM_CARD_TOKEN_2.setDeleted(true);
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2, testBeans.TKM_CARD_TOKEN_3
        ));
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1))
                .thenReturn(testBeans.TKM_CARD_PAN_1);
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        testBeans.READ_QUEUE_PAN_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(cardRepository).delete(DefaultBeans.encCard(testBeans.TKM_CARD_PAR_1));
        testBeans.TKM_CARD_PAN_PAR_1.setTokens(updatedTokens);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenPanParAndExistingParAndExistingPan_mergeCards() {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_3)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_3));
        testBeans.TKM_CARD_TOKEN_2.setDeleted(true);
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2, testBeans.TKM_CARD_TOKEN_3
        ));
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1))
                .thenReturn(null).thenReturn(testBeans.TKM_CARD_PAN_1);
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        testBeans.READ_QUEUE_PAN_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(cardRepository).delete(testBeans.TKM_CARD_PAN_1);
        testBeans.TKM_CARD_PAN_PAR_1.setTokens(updatedTokens);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenIncompleteCard_dontWriteOnQueue() throws JsonProcessingException {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1, true);
        verify(producerService, never()).sendMessage(Mockito.any(WriteQueue.class));
    }

    @Test
    void givenNewCompleteCard_writeOnQueue() throws JsonProcessingException {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_NEW_CARD);
    }

    @Test
    void givenUpdatedCard_writeOnQueue() throws JsonProcessingException {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_3)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_3));
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1).thenReturn(null);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_2, true);
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_UPDATED_CARD);
    }

    @Test
    void givenNotConsentCard_writeOnQueue() {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1).thenReturn(null);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Deny));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verifyNoInteractions(producerService);
    }

    @Test
    void givenExceptionOnCallToConsentClient_throwException() {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenThrow(FeignException.class);
        CardException cardException = Assertions.assertThrows(CardException.class, () -> cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true));
        Assertions.assertEquals(CALL_TO_CONSENT_MANAGER_FAILED, cardException.getErrorCode());
    }

    @Test
    void givenConsentNotFound_dontWriteOnQueue() throws JsonProcessingException {
        when(cryptoService.encrypt(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        FeignException.NotFound notFound = new FeignException.NotFound("", mock(Request.class), null);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenThrow(notFound);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(producerService, never()).sendMessage(Mockito.any(WriteQueue.class));
    }

}
