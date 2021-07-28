package it.gov.pagopa.tkm.ms.cardmanager.service;

import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.WalletsHashingEvaluationInput;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.response.WalletsHashingEvaluation;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenCardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.CardServiceImpl;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.CircuitBreakerManagerImpl;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.CryptoServiceImpl;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.ProducerServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.INCONSISTENT_MESSAGE;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestCardService {

    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardTokenRepository cardTokenRepository;

    @Mock
    private CitizenCardRepository citizenCardRepository;

    @Mock
    private ProducerServiceImpl producerService;

    @Mock
    private ConsentClient consentClient;

    @Mock
    private CryptoServiceImpl cryptoService;

    @Mock
    private RtdHashingClient rtdHashingClient;

    @Mock
    private CircuitBreakerManager circuitBreakerManager;

    private DefaultBeans testBeans;

    private final MockedStatic<Instant> instantMockedStatic = mockStatic(Instant.class);

    @BeforeEach
    void init() {
        testBeans = new DefaultBeans();
        instantMockedStatic.when(Instant::now).thenReturn(DefaultBeans.INSTANT);
        ReflectionTestUtils.setField(cardService, "apimRtdSubscriptionKey", "key");
        ReflectionTestUtils.setField(cardService, "circuitBreakerManager", new CircuitBreakerManagerImpl());

    }

    @AfterAll
    void close() {
        instantMockedStatic.close();
    }

    // ISSUER

    @Test
    void givenPan_returnHash() {
        testBeans.READ_QUEUE_PAR_TOKEN_1.getTokens().get(0).setHToken(null);
        when(rtdHashingClient.getHash(new WalletsHashingEvaluationInput(testBeans.TOKEN_1), "key")).thenReturn(new WalletsHashingEvaluation(testBeans.HTOKEN_1, "salt"));
        testBeans.TKM_CARD_TOKEN_1.setCard(testBeans.TKM_CARD_PAN_1);
        when(cardTokenRepository.findByHtokenAndDeletedFalse(testBeans.HTOKEN_1)).thenReturn(testBeans.TKM_CARD_TOKEN_1);
        when(cardRepository.findByPar(testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_TOKEN_1);
        verify(rtdHashingClient).getHash(new WalletsHashingEvaluationInput(testBeans.TOKEN_1), "key");
    }

    @Test
    void givenPan_invalidMEssage() {
        ReadQueue readQueue = ReadQueue.builder().build();
        CardException cardException = Assertions.assertThrows(CardException.class, () -> cardService.updateOrCreateCard(readQueue));
        Assertions.assertEquals(INCONSISTENT_MESSAGE, cardException.getErrorCode());
    }

    /*@Test
    void givenNewCard_persistNewCard() {
        when(cryptoService.encryptNullable(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenPanParAndExistingPanPar_doNothing() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        //todo
//        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenPanParAndExistingPan_updateCard() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        //todo
//        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenPanParAndExistingPar_updateCard() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        //todo
//        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenPanAndExistingPan_doNothing() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        //todo
//        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_1));
    }

    @Test
    void givenPanAndExistingPanPar_doNothing() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        //todo
//        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenParAndExistingPar_doNothing() {
        //todo
//        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_1, true);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAR_1));
    }

    @Test
    void givenParAndExistingPanPar_doNothing() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        //todo
//        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
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
        //todo
//        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        testBeans.READ_QUEUE_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_1, true);
        testBeans.TKM_CARD_PAR_1.setTokens(updatedTokens);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAR_1));
    }

    @Test
    void givenPanParAndExistingPanAndExistingPar_mergeCards() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_3)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_3));
        testBeans.TKM_CARD_TOKEN_2.setDeleted(true);
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2, testBeans.TKM_CARD_TOKEN_3
        ));
//        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1))
//                .thenReturn(testBeans.TKM_CARD_PAN_1);
        //todo
//        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        testBeans.READ_QUEUE_PAN_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(cardRepository).delete(DefaultBeans.encCard(testBeans.TKM_CARD_PAR_1));
        testBeans.TKM_CARD_PAN_PAR_1.setTokens(updatedTokens);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenPanParAndExistingParAndExistingPan_mergeCards() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_3)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_3));
        testBeans.TKM_CARD_TOKEN_2.setDeleted(true);
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2, testBeans.TKM_CARD_TOKEN_3
        ));
        //todo
//        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1))
//                .thenReturn(null).thenReturn(testBeans.TKM_CARD_PAN_1);
        //todo
//        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        testBeans.READ_QUEUE_PAN_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(cardRepository).delete(testBeans.TKM_CARD_PAN_1);
        testBeans.TKM_CARD_PAN_PAR_1.setTokens(updatedTokens);
        verify(cardRepository).save(DefaultBeans.encCard(testBeans.TKM_CARD_PAN_PAR_1));
    }

    @Test
    void givenIncompleteCard_dontWriteOnQueue() throws JsonProcessingException {
        when(cryptoService.encryptNullable(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1, true);
        verify(producerService, never()).sendMessage(Mockito.any(WriteQueue.class));
    }

    @Test
    void givenNewCompleteCard_writeOnQueue() throws JsonProcessingException {
        when(cryptoService.encryptNullable(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_NEW_CARD);
    }

    @Test
    void givenUpdatedCard_writeOnQueue() throws JsonProcessingException {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_3)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_3));
        //todo
//        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1).thenReturn(null);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_2, true);
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_UPDATED_CARD);
    }

    @Test
    void givenNotConsentCard_writeOnQueue() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        //todo
//        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1).thenReturn(null);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Deny));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verifyNoInteractions(producerService);
    }

    @Test
    void givenExceptionOnCallToConsentClient_throwException() {
        when(cryptoService.encryptNullable(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenThrow(FeignException.class);
        CardException cardException = Assertions.assertThrows(CardException.class, () -> cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true));
        Assertions.assertEquals(CALL_TO_CONSENT_MANAGER_FAILED, cardException.getErrorCode());
    }

    @Test
    void givenConsentNotFound_dontWriteOnQueue() throws JsonProcessingException {
        when(cryptoService.encryptNullable(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        FeignException.NotFound notFound = new FeignException.NotFound("", mock(Request.class), null);
        when(consentClient.getConsent(testBeans.TAX_CODE_1, testBeans.HPAN_1, null)).thenThrow(notFound);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1, true);
        verify(producerService, never()).sendMessage(Mockito.any(WriteQueue.class));
    }*/

    // NON-ISSUER

    @Test
    void givenParAndToken_givenExistingCardWithParAndToken_doNothing() {
        testBeans.TKM_CARD_TOKEN_1.setCard(testBeans.TKM_CARD_PAR_1);
        when(cardTokenRepository.findByHtokenAndDeletedFalse(testBeans.HTOKEN_1)).thenReturn(testBeans.TKM_CARD_TOKEN_1);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_TOKEN_1);
        verifyNoInteractions(cardRepository);
    }

    @Test
    void givenParAndToken_givenExistingCardWithPar_deleteTokenCardAndMergeTokens() {
        testBeans.TKM_CARD_TOKEN_1.setCard(testBeans.TKM_CARD_PAN_1);
        when(cardTokenRepository.findByHtokenAndDeletedFalse(testBeans.HTOKEN_1)).thenReturn(testBeans.TKM_CARD_TOKEN_1);
        when(cardRepository.findByPar(testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_TOKEN_1);
        verify(cardRepository).delete(testBeans.TKM_CARD_PAN_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAR_1);
    }

    @Test
    void givenParAndToken_givenExistingCardWithTokenAndNoCardWithPar_updatePar() {
        testBeans.TKM_CARD_TOKEN_1.setCard(testBeans.TKM_CARD_PAN_1);
        when(cardTokenRepository.findByHtokenAndDeletedFalse(testBeans.HTOKEN_1)).thenReturn(testBeans.TKM_CARD_TOKEN_1);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_TOKEN_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenParAndHpan_givenExistingCardWithParAndHpan_doNothing() {
        when(cardRepository.findByHpanAndPar(testBeans.HPAN_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_HPAN_1);
        verifyNoMoreInteractions(cardRepository, cardTokenRepository, citizenCardRepository);
    }

    @Test
    void givenParAndHpan_givenExistingCardWithHpan_updateCard() {
        testBeans.TKM_CARD_PAN_PAR_1.setLastUpdateDate(DefaultBeans.INSTANT);
        when(cardRepository.findByHpan(testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_HPAN_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenParAndHpan_givenExistingCardWithHpanAndExistingCardWithPar_mergeCards() {
        testBeans.TKM_CARD_PAN_PAR_1.setTokens(testBeans.TKM_CARD_TOKENS_ALL);
        testBeans.TKM_CARD_PAN_PAR_1.setLastUpdateDate(DefaultBeans.INSTANT);
        testBeans.TKM_CARD_PAR_1.setTokens(testBeans.TKM_CARD_TOKENS_2);
        testBeans.TKM_CARD_PAR_1.setId(1L);
        when(cardRepository.findByHpan(testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        when(cardRepository.findByPar(testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        when(citizenCardRepository.findByCardId(1L)).thenReturn(testBeans.CITIZEN_CARDS);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_HPAN_1);
        verify(cardRepository).delete(testBeans.TKM_CARD_PAR_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
        verify(citizenCardRepository).saveAll(testBeans.CITIZEN_CARDS_UPDATED);
    }

    @Test
    void givenParAndHpan_givenExistingCardWithPar_updateCard() {
        testBeans.TKM_CARD_PAN_PAR_1.setPan(null);
        testBeans.TKM_CARD_PAN_PAR_1.setLastUpdateDate(DefaultBeans.INSTANT);
        when(cardRepository.findByPar(testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_HPAN_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenParAndHpan_givenNoExistingCards_createCard() {
        testBeans.TKM_CARD_PAN_PAR_1.setPan(null);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_HPAN_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenToken_givenExistingToken_doNothing() {
        when(cardTokenRepository.findByHtokenAndDeletedFalse(testBeans.HTOKEN_1)).thenReturn(testBeans.TKM_CARD_TOKEN_1);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_TOKEN_1);
        verifyNoInteractions(cardRepository);
    }

    @Test
    void givenToken_givenNoExistingToken_createTokenAndFakeCard() {
        String encToken = DefaultBeans.enc(testBeans.TOKEN_1);
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(encToken);
        TkmCard fakeCard = TkmCard.builder().circuit(CircuitEnum.AMEX).tokens(Collections.singleton(
                TkmCardToken.builder()
                        .token(encToken)
                        .htoken(testBeans.HTOKEN_1)
                        .build()))
                .creationDate(DefaultBeans.INSTANT)
                .build();
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_TOKEN_1);
        verify(cardRepository).save(fakeCard);
    }

}
