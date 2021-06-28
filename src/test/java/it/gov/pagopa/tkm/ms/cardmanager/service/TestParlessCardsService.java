package it.gov.pagopa.tkm.ms.cardmanager.service;

import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.ParlessCardResponse;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestParlessCardsService {

    @InjectMocks
    private ParlessCardsServiceImpl parlessCardsService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CryptoServiceImpl cryptoService;

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
        when(cryptoService.decryptNullable(testBeans.PAN_1)).thenReturn(DefaultBeans.dec(testBeans.PAN_1));
        when(cryptoService.decryptNullable(testBeans.PAN_2)).thenReturn(DefaultBeans.dec(testBeans.PAN_2));
        when(cryptoService.decrypt(testBeans.TOKEN_1)).thenReturn(DefaultBeans.dec(testBeans.TOKEN_1));
        when(cryptoService.decrypt(testBeans.TOKEN_2)).thenReturn(DefaultBeans.dec(testBeans.TOKEN_2));
        //todo
//        when(cardRepository.findByParIsNullAndDeletedFalseAndLastReadDateBeforeOrParIsNullAndDeletedFalseAndLastReadDateIsNull(any(), any())).thenReturn(testBeans.TKM_CARD_LIST);
        List<ParlessCardResponse> parlessCards = parlessCardsService.getParlessCards(2);
        verify(cardRepository).saveAll(testBeans.TKM_CARD_LIST);
        assertEquals(parlessCards, testBeans.PARLESS_CARD_LIST);
    }

}
