package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCitizen;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCitizenCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete.DeleteQueueMessage;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenCardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.DeleteCardServiceImpl;
import org.apache.tomcat.util.bcel.Const;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.mockStatic;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestDeleteCardService {
    @InjectMocks
    private DeleteCardServiceImpl deleteCardService;

    @Mock
    private CitizenCardRepository citizenCardRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CitizenRepository citizenRepository;


    private final ObjectMapper testMapper = new ObjectMapper();

    @BeforeEach()
    void init() {
        testMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void deleteCard_happyFlow() {
        Instant creationDate = Instant.now();
        TkmCitizenCard tkmCitizenCard = TkmCitizenCard.builder().creationDate(creationDate)
                .deleted(false)
                .card(CardRepositoryMock.getTkmCardFull())
                .citizen(CitizenRepositoryMock.getCitizenFull(creationDate)).build();

        Instant deletedInstant = Instant.now();
        DeleteQueueMessage build = DeleteQueueMessage.builder()
                .timestamp(deletedInstant)
                .hpan(tkmCitizenCard.getCard().getHpan())
                .taxCode(tkmCitizenCard.getCitizen().getTaxCode()).build();

        Mockito.when(citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(Mockito.anyString(), Mockito.anyString())).thenReturn(tkmCitizenCard);
        deleteCardService.deleteCard(build);
        TkmCitizenCard tkmCitizenCardDeleted = TkmCitizenCard.builder().creationDate(creationDate)
                .deleted(true)
                .lastUpdateDate(deletedInstant)
                .card(CardRepositoryMock.getTkmCardFull())
                .citizen(CitizenRepositoryMock.getCitizenFull(creationDate)).build();
        Mockito.verify(citizenCardRepository).save(tkmCitizenCardDeleted);
    }

    @Test
    void deleteCard_noCardFoundWithCitizenAndCard() {
        Instant creationDate = Instant.now();
        TkmCard tkmCardFull = CardRepositoryMock.getTkmCardFull();
        TkmCitizen citizenFull = CitizenRepositoryMock.getCitizenFull(creationDate);

        Instant deletedInstant = Instant.now();
        DeleteQueueMessage build = DeleteQueueMessage.builder()
                .timestamp(deletedInstant)
                .hpan(tkmCardFull.getHpan())
                .taxCode(citizenFull.getTaxCode()).build();

        Mockito.when(citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        Mockito.when(cardRepository.findByHpan(Mockito.anyString())).thenReturn(tkmCardFull);
        Mockito.when(citizenRepository.findByTaxCodeAndDeletedFalse(Mockito.anyString())).thenReturn(citizenFull);
        deleteCardService.deleteCard(build);
        TkmCitizenCard citizenCard = TkmCitizenCard.builder().card(tkmCardFull).citizen(citizenFull).deleted(true)
                .lastUpdateDate(deletedInstant).creationDate(deletedInstant).build();
        Mockito.verify(citizenCardRepository).save(citizenCard);
    }

    @Test
    void deleteCard_noCardFoundWithCitizen() {
        try(MockedStatic<Instant> instantMockedStatic = mockStatic(Instant.class)) {
            instantMockedStatic.when(Instant::now).thenReturn(DefaultBeans.INSTANT);
            Instant creationDate = Instant.now();
            TkmCard tkmCardFull = TkmCard.builder().circuit(CircuitEnum.DELETED).hpan(Constant.HASH_1).creationDate(creationDate).build();
            TkmCitizen citizenFull = CitizenRepositoryMock.getCitizenFull(creationDate);

            Instant deletedInstant = Instant.now();
            DeleteQueueMessage build = DeleteQueueMessage.builder()
                    .timestamp(deletedInstant)
                    .hpan(tkmCardFull.getHpan())
                    .taxCode(citizenFull.getTaxCode()).build();

            Mockito.when(citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
            Mockito.when(cardRepository.findByHpan(Mockito.anyString())).thenReturn(null);
            Mockito.when(citizenRepository.findByTaxCodeAndDeletedFalse(Mockito.anyString())).thenReturn(citizenFull);
            deleteCardService.deleteCard(build);
            TkmCitizenCard citizenCard = TkmCitizenCard.builder().card(tkmCardFull).citizen(citizenFull).deleted(true)
                    .lastUpdateDate(deletedInstant).creationDate(deletedInstant).build();
            Mockito.verify(citizenCardRepository).save(citizenCard);
        }
    }

    @Test
    void deleteCard_noCardFoundWithCard() {
        TkmCard tkmCardFull = CardRepositoryMock.getTkmCardFull();

        Instant deletedInstant = Instant.now();
        TkmCitizen citizenFull = TkmCitizen.builder().taxCode(Constant.TAX_CODE_1).creationDate(deletedInstant).build();
        DeleteQueueMessage build = DeleteQueueMessage.builder()
                .timestamp(deletedInstant)
                .hpan(tkmCardFull.getHpan())
                .taxCode(citizenFull.getTaxCode()).build();

        Mockito.when(citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        Mockito.when(cardRepository.findByHpan(Mockito.anyString())).thenReturn(tkmCardFull);
        Mockito.when(citizenRepository.findByTaxCodeAndDeletedFalse(Mockito.anyString())).thenReturn(null);
        deleteCardService.deleteCard(build);
        TkmCitizenCard citizenCard = TkmCitizenCard.builder().card(tkmCardFull).citizen(citizenFull).deleted(true)
                .lastUpdateDate(deletedInstant).creationDate(deletedInstant).build();
        Mockito.verify(citizenCardRepository).save(citizenCard);
    }


}