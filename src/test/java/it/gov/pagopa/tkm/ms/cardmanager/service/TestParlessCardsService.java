package it.gov.pagopa.tkm.ms.cardmanager.service;

import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("WeakerAccess")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class TestParlessCardsService {

    @InjectMocks
    private ParlessCardsServiceImpl parlessCardsService;

    @Mock
    private CardRepository cardRepository;

    private DefaultBeans testBeans;

    @BeforeEach
    public void init() {
        testBeans = new DefaultBeans();
    }

    @Test
    public void givenMaxNumberOfCards_returnParlessCardsResponse() {
        int maxRecords = 2;
        when(cardRepository.findByParIsNullAndDeletedFalseAndLastReadDateBeforeOrParIsNullAndDeletedFalseAndLastReadDateIsNull(notNull(), notNull())).thenReturn(testBeans.TKM_CARD_LIST);
        List<ParlessCardResponse> parlessCards = parlessCardsService.getParlessCards(maxRecords);
        verify(cardRepository).saveAll(testBeans.TKM_CARD_LIST);
        assertEquals(parlessCards, testBeans.PARLESS_CARD_LIST);
    }

}
