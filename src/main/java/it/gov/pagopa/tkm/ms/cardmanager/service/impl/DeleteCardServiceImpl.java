package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import it.gov.pagopa.tkm.ms.cardmanager.exception.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete.DeleteQueueMessage;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenCardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CitizenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.DeleteCardService;
import it.gov.pagopa.tkm.util.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.MESSAGE_WRITE_FAILED;
import static it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.CardActionEnum.REVOKE;

@Log4j2
@Service
public class DeleteCardServiceImpl implements DeleteCardService {

    @Autowired
    private CitizenCardRepository citizenCardRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private ProducerServiceImpl producerService;

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
            tkmCitizenCard = TkmCitizenCard.builder().card(tkmCard).citizen(tkmCitizen).deleted(true).lastUpdateDate(timestamp).creationDate(timestamp).build();
            citizenCardRepository.save(tkmCitizenCard);
        } else {
            tkmCitizenCard.setDeleted(true);
            tkmCitizenCard.setLastUpdateDate(timestamp);
            citizenCardRepository.save(tkmCitizenCard);
            log.info("Deleted card with hpan " + ObfuscationUtils.obfuscateHpan(hpan));
        }
        if (!citizenCardRepository.existsByDeletedFalseAndCard_Hpan(hpan)) {
            log.info("This card is no longer associated with any citizen, sending REVOKE to write queue...");
            writeOnQueue(tkmCitizenCard);
        }
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

    private void writeOnQueue(TkmCitizenCard citizenCard) {
        TkmCard card = citizenCard.getCard();
        try {
            WriteQueueCard writeQueueCard = new WriteQueueCard(
                    card.getHpan(),
                    REVOKE,
                    card.getPar(),
                    null
            );
            WriteQueue writeQueue = new WriteQueue(
                    null,
                    Instant.now(),
                    Collections.singleton(writeQueueCard)
            );
            producerService.sendMessage(writeQueue);
        } catch (Exception e) {
            log.error("Error writing to queue for card hpan " + ObfuscationUtils.obfuscateHpan(card.getHpan()) + " par " + ObfuscationUtils.obfuscatePar(card.getPar()), e);
            throw new CardException(MESSAGE_WRITE_FAILED);
        }
    }

}
