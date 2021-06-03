package it.gov.pagopa.tkm.ms.cardmanager.service;

import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.ParlessCardResponse;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.ParlessCardsServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("WeakerAccess")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class TestParlessCardsService {

    @InjectMocks
    private ParlessCardsServiceImpl parlessCardsService;

    @Mock
    private CardRepository cardRepository;

    private DefaultBeans testBeans;

    private final MockedStatic<Instant> instantMockedStatic = mockStatic(Instant.class);

    @BeforeEach
    public void init() {
        testBeans = new DefaultBeans();
        instantMockedStatic.when(Instant::now).thenReturn(Instant.MAX);
    }

    @AfterAll
    public void close(){
        instantMockedStatic.close();
    }

    @Test
    public void givenMaxNumberOfCards_returnParlessCardsResponse() {
        int maxRecords = 2;
        when(cardRepository.findByParIsNullAndDeletedFalseAndLastReadDateBeforeOrParIsNullAndDeletedFalseAndLastReadDateIsNull(any(), any())).thenReturn(testBeans.TKM_CARD_LIST);
        List<ParlessCardResponse> parlessCards = parlessCardsService.getParlessCards(maxRecords);
        verify(cardRepository).saveAll(testBeans.TKM_CARD_LIST);
        assertEquals(parlessCards, testBeans.PARLESS_CARD_LIST);
    }

}
