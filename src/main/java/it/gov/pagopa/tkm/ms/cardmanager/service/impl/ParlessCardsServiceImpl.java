package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.service.ParlessCardsService;
import lombok.extern.log4j.*;
import org.apache.commons.collections.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ParlessCardsServiceImpl implements ParlessCardsService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CryptoServiceImpl cryptoService;

    @Override
    public List<ParlessCardResponse> getParlessCards(Integer maxRecords) {
        log.info("Getting parless cards with a limit of " + maxRecords + " cards");
        List<TkmCard> parlessCards = cardRepository.findByParIsNullAndHpanIsNotNullAndLastReadDateBeforeOrParIsNullAndHpanIsNotNullAndLastReadDateIsNull(
                Instant.now().minus(1, ChronoUnit.DAYS),
                PageRequest.of(0, maxRecords));
        for (TkmCard c : parlessCards) {
            c.setLastReadDate(Instant.now());
            c.getTokens().forEach(t -> t.setLastReadDate(Instant.now()));
        }
        cardRepository.saveAll(parlessCards);
        log.info("Found " + CollectionUtils.size(parlessCards) + " parless cards");
        return parlessCards.stream().map(c ->
                new ParlessCardResponse(
                        cryptoService.decryptNullable(c.getPan()),
                        c.getHpan(),
                        c.getCircuit(),
                        c.getTokens().stream().map(this::toParlessCardToken).collect(Collectors.toSet()))
        ).collect(Collectors.toList());
    }

    private ParlessCardToken toParlessCardToken(TkmCardToken token) {
        return new ParlessCardToken(cryptoService.decrypt(token.getToken()), token.getHtoken());
    }

}
