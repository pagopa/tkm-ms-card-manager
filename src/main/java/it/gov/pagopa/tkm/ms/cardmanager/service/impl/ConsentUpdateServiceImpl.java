package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.fasterxml.jackson.core.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.exception.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import static it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.CardActionEnum.INSERT_UPDATE;
import static it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.CardActionEnum.REVOKE;

@Service
public class ConsentUpdateServiceImpl implements ConsentUpdateService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ProducerServiceImpl producerService;

    @Override
    public void updateConsent(ConsentResponse consent) {
        String taxCode = consent.getTaxCode();
        List<TkmCard> cardsToUpdate = ConsentEntityEnum.Partial.equals(consent.getConsent()) ?
                cardRepository.findByTaxCodeAndHpanInAndParIsNotNullAndDeletedFalse(taxCode, consent.getHpans())
                : cardRepository.findByTaxCodeAndParIsNotNullAndDeletedFalse(taxCode);
        if (CollectionUtils.isEmpty(cardsToUpdate)) {
            return;
        }
        try {
            writeOnQueueIfComplete(cardsToUpdate, consent);
        } catch (Exception e) {
            throw new CardException(ErrorCodeEnum.MESSAGE_WRITE_FAILED);
        }
    }

    private void writeOnQueueIfComplete(List<TkmCard> cards, ConsentResponse consent) throws JsonProcessingException {
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
    }

}
