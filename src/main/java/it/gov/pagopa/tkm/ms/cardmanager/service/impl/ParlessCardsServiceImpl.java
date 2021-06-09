package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.ParlessCardResponse;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.ParlessCardsService;
import lombok.extern.log4j.*;
import org.apache.commons.collections.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ParlessCardsServiceImpl implements ParlessCardsService {

    @Autowired
    private CardRepository cardRepository;

    @Override
    public List<ParlessCardResponse> getParlessCards(Integer maxRecords) {
        log.info("Getting parless cards with a limit of " + maxRecords + " cards");
        List<TkmCard> parlessCards = cardRepository.findByParIsNullAndDeletedFalseAndLastReadDateBeforeOrParIsNullAndDeletedFalseAndLastReadDateIsNull(
                Instant.now().minus(1, ChronoUnit.DAYS),
                PageRequest.of(0, maxRecords));
        parlessCards.forEach(c -> c.setLastReadDate(Instant.now()));
        cardRepository.saveAll(parlessCards);
        log.info("Found " + CollectionUtils.size(parlessCards) + " parless cards");
        return parlessCards.stream().map(c ->
                new ParlessCardResponse(
                        c.getTaxCode(),
                        c.getPan(),
                        c.getTokens().stream().map(TkmCardToken::getToken).collect(Collectors.toSet()),
                        c.getCircuit())
        ).collect(Collectors.toList());
    }
}
