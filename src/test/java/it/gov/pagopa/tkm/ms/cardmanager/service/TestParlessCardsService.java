package it.gov.pagopa.tkm.ms.cardmanager.service;

import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestParlessCardsService {

    @InjectMocks
    private ParlessCardsServiceImpl parlessCardsService;

    @Mock
    private CardRepository cardRepository;

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
    void givenMaxNumberOfCards_returnParlessCardsResponse() {
        when(cardRepository.findByParIsNullAndLastReadDateBeforeAndCircuitNotAndPanIsNotNullOrParIsNullAndLastReadDateIsNullAndCircuitNotAndPanIsNotNull(any(), any(), any(), any())).thenReturn(testBeans.TKM_CARD_LIST);
        parlessCardsService.getParlessCards(2);
        verify(cardRepository).saveAll(testBeans.TKM_CARD_LIST);
    }

}
