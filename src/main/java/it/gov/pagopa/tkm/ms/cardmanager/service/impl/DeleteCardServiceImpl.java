package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCitizen;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCitizenCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete.DeleteQueueMessage;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.TkmCitizenCardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.DeleteCardService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Log4j2
@Service
public class DeleteCardServiceImpl implements DeleteCardService {

    @Autowired
    private TkmCitizenCardRepository tkmCitizenCardRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CitizenRepository citizenRepository;

    @Override
    public void deleteCard(DeleteQueueMessage deleteQueueMessage) {
        String taxCode = deleteQueueMessage.getTaxCode();
        String hpan = deleteQueueMessage.getHpan();
        TkmCitizenCard tkmCitizenCard = tkmCitizenCardRepository.findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(taxCode, hpan);
        Instant timestamp = deleteQueueMessage.getTimestamp();
        if (tkmCitizenCard == null) {
            log.info(String.format("No Card found with hpan %s and taxCode %s. Creating new record", hpan, taxCode));
            TkmCard tkmCard = getTkmCard(hpan);
            TkmCitizen tkmCitizen = getTkmCitizen(taxCode, timestamp);
            TkmCitizenCard citizenCard = TkmCitizenCard.builder().card(tkmCard).citizen(tkmCitizen).deleted(true)
                    .lastUpdateDate(timestamp).creationDate(timestamp).build();
            tkmCitizenCardRepository.save(citizenCard);
            return;
        }
        tkmCitizenCard.setDeleted(true);
        tkmCitizenCard.setLastUpdateDate(timestamp);
        tkmCitizenCardRepository.save(tkmCitizenCard);
        log.info(String.format("Deleted card with hpan %s and taxCode %s", hpan, taxCode));
    }

    private TkmCard getTkmCard(String hpan) {
        TkmCard byHpan = cardRepository.findByHpan(hpan);
        if (byHpan == null) {
            byHpan = TkmCard.builder().circuit(CircuitEnum.DELETED).hpan(hpan).build();
        }
        return byHpan;
    }

    private TkmCitizen getTkmCitizen(String taxCode, Instant instant) {
        TkmCitizen byTaxCode = citizenRepository.findByTaxCodeAndDeletedFalse(taxCode);
        if (byTaxCode == null) {
            byTaxCode = TkmCitizen.builder().taxCode(taxCode).creationDate(instant).build();
        }
        return byTaxCode;
    }
}
