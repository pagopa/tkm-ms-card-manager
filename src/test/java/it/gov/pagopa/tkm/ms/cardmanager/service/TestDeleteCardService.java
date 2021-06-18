package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.tkm.ms.cardmanager.constant.CardRepositoryMock;
import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete.DeleteQueueMessage;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.DeleteCardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestDeleteCardService {
    @InjectMocks
    private DeleteCardServiceImpl deleteCardService;

    @Mock
    private CardRepository cardRepository;
    private DefaultBeans testBeans;

    private final ObjectMapper testMapper = new ObjectMapper();

    @BeforeEach()
    void init() {
        testBeans = new DefaultBeans();
        testMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void deleteCard_happyFlow() {
        TkmCard tkmCardFull = CardRepositoryMock.getTkmCardFull();
        DeleteQueueMessage build = DeleteQueueMessage.builder()
                .timestamp(Instant.now())
                .hpan(tkmCardFull.getHpan())
                .taxCode(tkmCardFull.getTaxCode()).build();
        Mockito.when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(Mockito.anyString(), Mockito.anyString())).thenReturn(tkmCardFull);
        deleteCardService.deleteCard(build);

        tkmCardFull.setDeleted(true);
        tkmCardFull.getTokens().forEach(t -> t.setDeleted(true));
        Mockito.verify(cardRepository).save(tkmCardFull);
    }

    @Test
    void deleteCard_noCardFound() {
        TkmCard tkmCardFull = CardRepositoryMock.getTkmCardFull();
        DeleteQueueMessage build = DeleteQueueMessage.builder()
                .timestamp(Instant.now())
                .hpan(tkmCardFull.getHpan())
                .taxCode(tkmCardFull.getTaxCode()).build();
        Mockito.when(cardRepository.findByTaxCodeAndHpanAndDeletedFalse(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        deleteCardService.deleteCard(build);
        Mockito.verify(cardRepository, Mockito.never()).save(Mockito.any());
    }
}