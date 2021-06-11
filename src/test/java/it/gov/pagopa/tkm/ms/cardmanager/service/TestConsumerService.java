package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueue;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.ConsumerServiceImpl;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.ProducerServiceImpl;
import it.gov.pagopa.tkm.service.PgpUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.Validation;
import javax.validation.Validator;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.MESSAGE_DECRYPTION_FAILED;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.MESSAGE_VALIDATION_FAILED;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestConsumerService {

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Mock
    private PgpUtils pgpUtils;

    @Mock
    private CardRepository cardRepository;

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ProducerServiceImpl producerService;

    private DefaultBeans testBeans;

    private final MockedStatic<Instant> instantMockedStatic = mockStatic(Instant.class);

    private final ObjectMapper testMapper = new ObjectMapper();

    @BeforeEach
    void init() {
        testBeans = new DefaultBeans();
        instantMockedStatic.when(Instant::now).thenReturn(DefaultBeans.INSTANT);
    }

    @AfterAll
    void close() {
        instantMockedStatic.close();
    }

    private void startupAssumptions(ReadQueue readQueueToTest) throws Exception {
        String readQueueAsString = testMapper.writeValueAsString(readQueueToTest);
        when(pgpUtils.decrypt("MESSAGE")).thenReturn(readQueueAsString);
        when(mapper.readValue(readQueueAsString, ReadQueue.class)).thenReturn(readQueueToTest);
    }

    @Test
    void givenNewCard_InvalidMessageFormat() throws Exception {
        String message = "MESSAGE";
        when(pgpUtils.decrypt(Mockito.anyString())).thenReturn(message);
        when(mapper.readValue(Mockito.anyString(), Mockito.eq(ReadQueue.class))).thenReturn(new ReadQueue());
        CardException cardException = Assertions.assertThrows(CardException.class, () -> consumerService.consume(message));
        Assertions.assertEquals(MESSAGE_VALIDATION_FAILED, cardException.getErrorCode());
    }

    @Test
    void givenNewCard_InvalidMessageEncryption() throws Exception {
        String message = "MESSAGE";
        when(pgpUtils.decrypt(Mockito.anyString())).thenThrow(new Exception());
        CardException cardException = Assertions.assertThrows(CardException.class, () -> consumerService.consume(message));
        Assertions.assertEquals(MESSAGE_DECRYPTION_FAILED, cardException.getErrorCode());
    }

    @Test
    void givenNewCard_persistNewCard() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_1);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(null);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenPanParAndExistingPanPar_doNothing() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_1);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenPanParAndExistingPan_updateCard() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_1);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(null);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenPanParAndExistingPar_updateCard() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_1);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenPanAndExistingPan_doNothing() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_1);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_1);
    }

    @Test
    void givenPanAndExistingPanPar_doNothing() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_1);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenParAndExistingPar_doNothing() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAR_1);
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAR_1);
    }

    @Test
    void givenParAndExistingPanPar_doNothing() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAR_1);
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    void givenNewTokensAndExistingCard_replaceTokensIfNew() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2));
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2.setDeleted(true), testBeans.TKM_CARD_TOKEN_3
        ));
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAR_1.setTokens(updatedTokens));
    }

    @Test
    void givenPanParAndExistingPanAndExistingPar_mergeCards() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2));
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2.setDeleted(true), testBeans.TKM_CARD_TOKEN_3
        ));
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1))
                .thenReturn(testBeans.TKM_CARD_PAN_1);
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        consumerService.consume("MESSAGE");
        verify(cardRepository).delete(testBeans.TKM_CARD_PAR_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1.setTokens(updatedTokens));
    }

    @Test
    void givenPanParAndExistingParAndExistingPan_mergeCards() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2));
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2.setDeleted(true), testBeans.TKM_CARD_TOKEN_3
        ));
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1))
                .thenReturn(null).thenReturn(testBeans.TKM_CARD_PAN_1);
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1);
        consumerService.consume("MESSAGE");
        verify(cardRepository).delete(testBeans.TKM_CARD_PAN_1);
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1.setTokens(updatedTokens));
    }

    @Test
    void givenIncompleteCard_dontWriteOnQueue() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_1);
        consumerService.consume("MESSAGE");
        verify(producerService, never()).sendMessage(Mockito.any(WriteQueue.class));
    }

    @Test
    void givenNewCompleteCard_writeOnQueue() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_1);
        consumerService.consume("MESSAGE");
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_NEW_CARD);
    }

    @Test
    void givenUpdatedCard_writeOnQueue() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_2);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1).thenReturn(null);
        consumerService.consume("MESSAGE");
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_UPDATED_CARD);
    }

}
