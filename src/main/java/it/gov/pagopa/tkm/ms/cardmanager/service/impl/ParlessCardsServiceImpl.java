package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.*;

@Service
public class ParlessCardsServiceImpl implements ParlessCardsService {

    @Autowired
    private CardRepository cardRepository;

    @Override
    public List<ParlessCardResponse> getParlessCards(Integer maxRecords) {
        List<TkmCard> parlessCards = cardRepository.findByParIsNullAndDeletedFalseAndLastReadDateBeforeOrParIsNullAndDeletedFalseAndLastReadDateIsNull(
                Instant.now().minus(1, ChronoUnit.DAYS),
                PageRequest.of(0, maxRecords));
        parlessCards.forEach(c -> c.setLastReadDate(Instant.now()));
        cardRepository.saveAll(parlessCards);
        return parlessCards.stream().map(c ->
                        new ParlessCardResponse(
                            c.getTaxCode(),
                            c.getPan(),
                            c.getTokens().stream().map(TkmCardToken::getToken).collect(Collectors.toSet()),
                            c.getCircuit())
        ).collect(Collectors.toList());
    }
}
