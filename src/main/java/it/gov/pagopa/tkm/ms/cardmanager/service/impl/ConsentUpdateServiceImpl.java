package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.exception.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.*;
import lombok.extern.log4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import static it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.CardActionEnum.INSERT_UPDATE;
import static it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.CardActionEnum.REVOKE;

@Service
@Log4j2
public class ConsentUpdateServiceImpl implements ConsentUpdateService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ProducerServiceImpl producerService;

    @Override
    public void updateConsent(ConsentResponse consent) {
        String taxCode = consent.getTaxCode();
        log.info("Updating consent for taxCode " + taxCode + " with value " + consent.getConsent());
        List<TkmCard> cardsToUpdate = ConsentEntityEnum.Partial.equals(consent.getConsent()) ?
                cardRepository.findByTaxCodeAndHpanInAndParIsNotNullAndDeletedFalse(taxCode, consent.retrieveHpans())
                : cardRepository.findByTaxCodeAndParIsNotNullAndDeletedFalse(taxCode);
        log.info("Cards to update: " + cardsToUpdate.stream().map(TkmCard::getHpan).collect(Collectors.joining(", ")));
        if (CollectionUtils.isEmpty(cardsToUpdate)) {
            return;
        }
        writeOnQueueIfComplete(cardsToUpdate, consent);
    }

    private void writeOnQueueIfComplete(List<TkmCard> cards, ConsentResponse consent) {
        try {
            Set<WriteQueueCard> writeQueueCards = cards.stream().map(card ->
                    new WriteQueueCard(
                                card.getHpan(),
                                consent.cardHasConsent(card.getHpan()) ? INSERT_UPDATE : REVOKE,
                                card.getPar(),
                                consent.cardHasConsent(card.getHpan()) ? card.getTokens().stream().map(WriteQueueToken::new).collect(Collectors.toSet()) : null
                        )
            ).collect(Collectors.toSet());
            WriteQueue writeQueue = new WriteQueue(
                    consent.getTaxCode(),
                    Instant.now(),
                    writeQueueCards
            );
            producerService.sendMessage(writeQueue);
        } catch (Exception e) {
            log.error(e);
            throw new CardException(ErrorCodeEnum.MESSAGE_WRITE_FAILED);
        }
    }

}
