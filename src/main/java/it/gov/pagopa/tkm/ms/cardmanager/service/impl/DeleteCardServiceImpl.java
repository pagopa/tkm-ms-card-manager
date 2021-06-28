package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete.DeleteQueueMessage;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.DeleteCardService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Log4j2
@Service
public class DeleteCardServiceImpl implements DeleteCardService {

    @Autowired
    private CardRepository cardRepository;

    @Override
    public void deleteCard(DeleteQueueMessage deleteQueueMessage) {
        String taxCode = deleteQueueMessage.getTaxCode();
        String hpan = deleteQueueMessage.getHpan();
//        TkmCard byTaxCodeAndHpanAndDeletedFalse = cardRepository.findByTaxCodeAndHpanAndDeletedFalse(taxCode, hpan);
//        Instant timestamp = deleteQueueMessage.getTimestamp();
//        if (byTaxCodeAndHpanAndDeletedFalse == null) {
//            log.info(String.format("No Card found with hpan %s and taxCode %s. Creating new record", hpan, taxCode));
//            TkmCard newCardAlreadyDeleted = TkmCard.builder().taxCode(taxCode).hpan(hpan).lastUpdateDate(timestamp).build();
//            cardRepository.save(newCardAlreadyDeleted);
//            return;
//        }
//
//        Set<TkmCardToken> tokens = byTaxCodeAndHpanAndDeletedFalse.getTokens();
//        tokens.forEach(t -> t.setDeleted(true));
//
//        byTaxCodeAndHpanAndDeletedFalse.setDeleted(true);
//        byTaxCodeAndHpanAndDeletedFalse.setLastUpdateDate(timestamp);
//        cardRepository.save(byTaxCodeAndHpanAndDeletedFalse);
//        log.info(String.format("Deleted card with hpan %s and taxCode %s", hpan, taxCode));
//todo
    }

}
