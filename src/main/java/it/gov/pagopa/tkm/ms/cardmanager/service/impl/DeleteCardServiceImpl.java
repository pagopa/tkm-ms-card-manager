package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCitizen;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCitizenCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete.DeleteQueueMessage;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenCardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.DeleteCardService;
import it.gov.pagopa.tkm.util.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Log4j2
@Service
public class DeleteCardServiceImpl implements DeleteCardService {

    @Autowired
    private CitizenCardRepository citizenCardRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CitizenRepository citizenRepository;

    @Override
    public void deleteCard(DeleteQueueMessage deleteQueueMessage) {
        String taxCode = deleteQueueMessage.getTaxCode();
        String hpan = deleteQueueMessage.getHpan();
        TkmCitizenCard tkmCitizenCard = citizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(taxCode, hpan);
        Instant timestamp = deleteQueueMessage.getTimestamp();
        if (tkmCitizenCard == null) {
            log.info("No Card found with hpan " + ObfuscationUtils.obfuscateHpan(hpan) + ". Creating new record");
            TkmCard tkmCard = getTkmCard(hpan);
            TkmCitizen tkmCitizen = getTkmCitizen(taxCode, timestamp);
            TkmCitizenCard citizenCard = TkmCitizenCard.builder().card(tkmCard).citizen(tkmCitizen).deleted(true)
                    .lastUpdateDate(timestamp).creationDate(timestamp).build();
            citizenCardRepository.save(citizenCard);
            return;
        }
        tkmCitizenCard.setDeleted(true);
        tkmCitizenCard.setLastUpdateDate(timestamp);
        citizenCardRepository.save(tkmCitizenCard);
        log.info("Deleted card with hpan " + ObfuscationUtils.obfuscateHpan(hpan));
    }

    private TkmCard getTkmCard(String hpan) {
        TkmCard card = cardRepository.findByHpan(hpan);
        if (card == null) {
            card = TkmCard.builder().circuit(CircuitEnum.DELETED).creationDate(Instant.now()).hpan(hpan).build();
        }
        card.setLastReadDate(null);
        return card;
    }

    private TkmCitizen getTkmCitizen(String taxCode, Instant instant) {
        TkmCitizen byTaxCode = citizenRepository.findByTaxCodeAndDeletedFalse(taxCode);
        if (byTaxCode == null) {
            byTaxCode = TkmCitizen.builder().taxCode(taxCode).creationDate(instant).build();
        }
        return byTaxCode;
    }
}
