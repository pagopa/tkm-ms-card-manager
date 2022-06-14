package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentEntityEnum;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentResponse;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueue;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenCardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.CardServiceImpl;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.CALL_TO_CONSENT_MANAGER_FAILED;
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
    private CitizenRepository citizenRepository;

    @Mock
    private ProducerServiceImpl producerService;

    @Mock
    private ConsentClient consentClient;

    @Mock
    private CryptoServiceImpl cryptoService;

    @Mock
    private RtdHashingClient rtdHashingClient;

    private DefaultBeans testBeans;

    @Mock
    private CircuitBreakerManager circuitBreakerManager;

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

    //NON-ISSUER

    @Test
    void givenParAndToken_givenExistingCardWithParAndToken_doNothing() {
        testBeans.TKM_CARD_TOKEN_1.setCard(testBeans.TKM_CARD_PAR_1);
        when(cardTokenRepository.findByHtokenAndDeletedFalse(testBeans.HTOKEN_1)).thenReturn(testBeans.TKM_CARD_TOKEN_1);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_TOKEN_1);
        verifyNoInteractions(cardRepository);
    }

    @Test
    void givenParAndToken_givenExistingCardWithParAndToken_deleteTokenCardAndMergeTokens() {
        testBeans.TKM_CARD_TOKEN_1.setCard(testBeans.TKM_CARD_PAN_1);
        when(cardTokenRepository.findByHtokenAndDeletedFalse(testBeans.HTOKEN_1)).thenReturn(testBeans.TKM_CARD_TOKEN_1);
        when(cardRepository.findByPar(testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_TOKEN_1);
        verify(cardRepository).delete(testBeans.TKM_CARD_PAN_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAR_1);
    }

    @Test
    void givenParAndToken_givenExistingCardWithPar_deleteTokenCardAndMergeTokens() {
        // in DB we have token1 linked to FAKE CARD without PAR
        testBeans.TKM_CARD_TOKEN_1.setCard(testBeans.FAKE_CARD);
        testBeans.FAKE_CARD.getTokens().add(testBeans.TKM_CARD_TOKEN_1);
        when(cardTokenRepository.findByHtokenAndDeletedFalse(testBeans.HTOKEN_1)).thenReturn(testBeans.TKM_CARD_TOKEN_1);

        // and we have another CARD with PAR and PAN not linked to token1
        testBeans.TKM_CARD_PAN_PAR_1.setTokens(new HashSet<>());
        when(cardRepository.findByPar(testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);

        // and we receive PAR for token1
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_TOKEN_1);

        verify(cardRepository).delete(testBeans.FAKE_CARD);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenPan_invalidMEssage() {
        ReadQueue readQueue = ReadQueue.builder().build();
        CardException cardException = Assertions.assertThrows(CardException.class, () -> cardService.updateOrCreateCard(readQueue));
        Assertions.assertEquals(INCONSISTENT_MESSAGE, cardException.getErrorCode());
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
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
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
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_2, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
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

    //ISSUER

    @Test
    void givenPan_returnHash() {
        when(cryptoService.encryptNullable(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(circuitBreakerManager.callRtdForHash(any(RtdHashingClient.class), anyString(), anyString())).thenReturn(testBeans.HPAN_1);
        testBeans.READ_QUEUE_PAN_1.setHpan(null);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1);
        verify(circuitBreakerManager).callRtdForHash(rtdHashingClient, testBeans.PAN_1, "key");
    }

    @Test
    void givenNewCard_persistNewCard() {
        when(cryptoService.encryptNullable(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verify(citizenCardRepository).save(testBeans.CITIZEN_CARD_PAN_PAR);
    }

    @Test
    void givenPanParAndExistingPanPar_doNothing() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(cardRepository.findByHpan(testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verify(citizenCardRepository).save(testBeans.CITIZEN_CARD_PAN_PAR);
    }

    @Test
    void givenPanParAndExistingPan_updateCard() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.CITIZEN_CARD_PAN);
        when(cardRepository.findByHpan(testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        testBeans.TKM_CARD_PAN_PAR_1.setLastUpdateDate(DefaultBeans.INSTANT);
        verify(citizenCardRepository).save(testBeans.CITIZEN_CARD_PAN_PAR);
    }

    @Test
    void givenPanParAndExistingPar_updateCard() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Par(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.CITIZEN_CARD_PAN_PAR);
        when(cardRepository.findByPar(testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        testBeans.TKM_CARD_PAN_PAR_1.setLastUpdateDate(DefaultBeans.INSTANT);
        verify(citizenCardRepository).save(testBeans.CITIZEN_CARD_PAN_PAR);
    }

    @Test
    void givenPanAndExistingPan_doNothing() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.CITIZEN_CARD_PAN);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1);
        verify(citizenCardRepository).save(testBeans.CITIZEN_CARD_PAN);
    }

    @Test
    void givenPanAndExistingPanPar_doNothing() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.CITIZEN_CARD_PAN_PAR);
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1);
        verify(citizenCardRepository).save(testBeans.CITIZEN_CARD_PAN_PAR);
    }

    @Test
    void givenParAndExistingPar_doNothing() {
        testBeans.TKM_CARD_PAR_1.setPan(null);
        when(cardRepository.findByPar(testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_1);
        verify(citizenCardRepository).save(testBeans.CITIZEN_CARD_PAR);
    }

    @Test
    void givenParAndExistingPanPar_doNothing() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(cardRepository.findByPar(testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_1);
        verify(citizenCardRepository).save(testBeans.CITIZEN_CARD_PAN_PAR);
    }

    @Test
    void givenNewTokensAndExistingCard_addNewTokens() {
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2, testBeans.TKM_CARD_TOKEN_3
        ));
        when(cardRepository.findByPar(testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        // TODO: to verify
        // when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, null)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        testBeans.READ_QUEUE_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_1);
        testBeans.TKM_CARD_PAR_1.setTokens(updatedTokens);
        verify(citizenCardRepository).save(testBeans.CITIZEN_CARD_PAR);
    }

    @Test
    void givenPanParAndExistingPanAndExistingPar_mergeCards() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_3)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_3));
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2, testBeans.TKM_CARD_TOKEN_3
        ));
        when(citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.CITIZEN_CARD_PAN);
        when(cardRepository.findByPar(testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        when(cardRepository.findByHpan(testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        testBeans.READ_QUEUE_PAN_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2);
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verify(cardRepository).delete(testBeans.TKM_CARD_PAR_1);
        testBeans.TKM_CARD_PAN_PAR_1.setTokens(updatedTokens);
        testBeans.TKM_CARD_PAN_PAR_1.setLastUpdateDate(DefaultBeans.INSTANT);
        verify(citizenCardRepository).save(testBeans.CITIZEN_CARD_PAN_PAR);
    }

    @Test
    void givenIncompleteCard_dontWriteOnQueue() throws JsonProcessingException {
        when(cryptoService.encryptNullable(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1);
        verify(producerService, never()).sendMessage(any(WriteQueue.class));
    }

    @Test
    void givenNewCompleteCard_writeOnQueue() throws JsonProcessingException {
        when(cryptoService.encryptNullable(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_NEW_CARD);
    }

// TODO: to verify
//    @Test
//    void givenUpdatedCard_writeOnQueue() throws JsonProcessingException {
//        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
//        when(cryptoService.encrypt(testBeans.TOKEN_3)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_3));
//        when(citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.CITIZEN_CARD_PAN_PAR).thenReturn(null);
//        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
//        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_2);
//        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_UPDATED_CARD);
//    }

    @Test
    void givenNotConsentCard_writeOnQueue() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(cardRepository.findByHpan(testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1).thenReturn(null);
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Deny));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verifyNoInteractions(producerService);
    }

    @Test
    void givenExceptionOnCallToConsentClient_throwException() {
        when(cryptoService.encryptNullable(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenThrow(new CardException(CALL_TO_CONSENT_MANAGER_FAILED));
        CardException cardException = Assertions.assertThrows(CardException.class, () -> cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1));
        Assertions.assertEquals(CALL_TO_CONSENT_MANAGER_FAILED, cardException.getErrorCode());
    }

    @Test
    void givenConsentNotFound_dontWriteOnQueue() throws JsonProcessingException {
        when(cryptoService.encryptNullable(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(ConsentResponse.builder().consent(ConsentEntityEnum.Deny).build());
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_PAR_1);
        verify(producerService, never()).sendMessage(any(WriteQueue.class));
    }

    @Test
    void givenTokenAndExistingPanAndToken_mergeCards() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        testBeans.TKM_CARD_PAN_1.setId(1L);
        testBeans.TKM_CARD_TOKEN_1.setCard(testBeans.TKM_CARD_PAN_1);
        when(cardTokenRepository.findByHtokenIn(Arrays.asList(testBeans.HTOKEN_1, testBeans.HTOKEN_2))).thenReturn(Collections.singletonList(testBeans.TKM_CARD_TOKEN_1));
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAR_1);
        verify(citizenCardRepository).findByCardIdIn(Collections.singletonList(1L));
        verify(citizenCardRepository).save(testBeans.CITIZEN_CARD_PAN_PAR);
        verify(cardRepository).deleteAll(Collections.singletonList(testBeans.TKM_CARD_PAN_1));
        verify(cardTokenRepository).saveAll(new HashSet<>(Arrays.asList(testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2)));
    }

    @Test
    void givenTokenAndExistingParAndToken_mergeCards() {
        when(cryptoService.encrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_1));
        when(cryptoService.encrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.enc(testBeans.TOKEN_2));
        // TODO: to verify
        when(cryptoService.encryptNullable(testBeans.PAN_1)).thenReturn(DefaultBeans.enc(testBeans.PAN_1));
        testBeans.TKM_CARD_PAR_1.setId(1L);
        testBeans.TKM_CARD_TOKEN_1.setCard(testBeans.TKM_CARD_PAR_1);
        when(cardTokenRepository.findByHtokenIn(Arrays.asList(testBeans.HTOKEN_1, testBeans.HTOKEN_2))).thenReturn(Collections.singletonList(testBeans.TKM_CARD_TOKEN_1));
        // TODO: to verify
        when(circuitBreakerManager.consentClientGetConsent(consentClient, testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow));
        cardService.updateOrCreateCard(testBeans.READ_QUEUE_PAN_1);
        verify(citizenCardRepository).findByCardIdIn(Collections.singletonList(1L));
        verify(citizenCardRepository).save(testBeans.CITIZEN_CARD_PAN_PAR);
        verify(cardRepository).deleteAll(Collections.singletonList(testBeans.TKM_CARD_PAR_1));
        verify(cardTokenRepository).saveAll(new HashSet<>(Arrays.asList(testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2)));
    }

}