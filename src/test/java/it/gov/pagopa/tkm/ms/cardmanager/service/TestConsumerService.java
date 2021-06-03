package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.databind.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.hash.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import it.gov.pagopa.tkm.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import javax.validation.*;

import java.time.*;
import java.util.*;

import static org.mockito.Mockito.*;

@SuppressWarnings("WeakerAccess")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class TestConsumerService {

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Mock
    private PgpUtils pgpUtils;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ApimClient apimClient;

    @Mock
    private Validator validator;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ProducerServiceImpl producerService;

    private DefaultBeans testBeans;

    private final MockedStatic<Instant> instantMockedStatic = mockStatic(Instant.class);

    private final ObjectMapper testMapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        testBeans = new DefaultBeans();
        instantMockedStatic.when(Instant::now).thenReturn(testBeans.INSTANT);
    }

    @AfterAll
    public void close(){
        instantMockedStatic.close();
    }

    private void startupAssumptions(ReadQueue readQueueToTest) throws Exception {
        String readQueueAsString = testMapper.writeValueAsString(readQueueToTest);
        when(pgpUtils.decrypt("MESSAGE")).thenReturn(readQueueAsString);
        when(mapper.readValue(readQueueAsString, ReadQueue.class)).thenReturn(readQueueToTest);
        when(validator.validate(readQueueToTest)).thenReturn(new HashSet<>());
    }

    @Test
    public void givenNewCard_persistNewCard() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_1);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(null);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    public void givenPanParAndExistingPanPar_doNothing() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_1);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    public void givenPanParAndExistingPan_updateCard() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_1);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(null);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    public void givenPanParAndExistingPar_updateCard() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_1);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    public void givenPanAndExistingPan_doNothing() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_1);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_1);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_1);
    }

    @Test
    public void givenPanAndExistingPanPar_doNothing() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_1);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    public void givenParAndExistingPar_doNothing() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAR_1);
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAR_1);
    }

    @Test
    public void givenParAndExistingPanPar_doNothing() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAR_1);
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAN_PAR_1);
    }

    @Test
    public void givenNewTokensAndExistingCard_replaceTokensIfNew() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAR_1.setTokens(testBeans.QUEUE_TOKEN_LIST_2));
        Set<TkmCardToken> updatedTokens = new HashSet<>(Arrays.asList(
                testBeans.TKM_CARD_TOKEN_1, testBeans.TKM_CARD_TOKEN_2.setDeleted(true), testBeans.TKM_CARD_TOKEN_3
        ));
        when(cardRepository.findByTaxCodeAndParAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.PAR_1)).thenReturn(testBeans.TKM_CARD_PAR_1).thenReturn(null);
        consumerService.consume("MESSAGE");
        verify(cardRepository).save(testBeans.TKM_CARD_PAR_1.setTokens(updatedTokens));
    }

    @Test
    public void givenPanParAndExistingPanAndExistingPar_mergeCards() throws Exception {
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
    public void givenPanParAndExistingParAndExistingPan_mergeCards() throws Exception {
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
    public void givenIncompleteCard_dontWriteOnQueue() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_1);
        consumerService.consume("MESSAGE");
        verify(producerService, never()).sendMessage(Mockito.any(WriteQueue.class));
    }

    @Test
    public void givenNewCompleteCard_writeOnQueue() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_1);
        consumerService.consume("MESSAGE");
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_NEW_CARD);
    }

    @Test
    public void givenUpdatedCard_writeOnQueue() throws Exception {
        startupAssumptions(testBeans.READ_QUEUE_PAN_PAR_2);
        when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(testBeans.TAX_CODE_1, testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1).thenReturn(null);
        consumerService.consume("MESSAGE");
        verify(producerService).sendMessage(testBeans.WRITE_QUEUE_FOR_UPDATED_CARD);
    }

}
