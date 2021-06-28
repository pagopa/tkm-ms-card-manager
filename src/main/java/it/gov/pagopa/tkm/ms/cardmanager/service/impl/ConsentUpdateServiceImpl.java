package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentResponse;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueueCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueueToken;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.ConsentUpdateService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<TkmCard> cardsToUpdate=null;//todo
//        List<TkmCard> cardsToUpdate = ConsentEntityEnum.Partial.equals(consent.getConsent()) ?
//                cardRepository.findByTaxCodeAndHpanInAndParIsNotNullAndDeletedFalse(taxCode, consent.retrieveHpans())
//                : cardRepository.findByTaxCodeAndParIsNotNullAndDeletedFalse(taxCode);
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
