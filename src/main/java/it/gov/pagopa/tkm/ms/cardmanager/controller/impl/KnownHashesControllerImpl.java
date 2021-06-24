package it.gov.pagopa.tkm.ms.cardmanager.controller.impl;

import it.gov.pagopa.tkm.ms.cardmanager.controller.KnownHashesController;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import lombok.extern.log4j.*;
import org.apache.commons.collections.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@Log4j2
public class KnownHashesControllerImpl implements KnownHashesController {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardTokenRepository cardTokenRepository;

    @Override
    public KnownHashesResponse getKnownHashes(Long maxRecords, Long hpanOffset, Long htokenOffset) {
        log.info("Retrieving a maximum of " + maxRecords + " hashes with hpan offset " + hpanOffset + " and htoken offset " + htokenOffset);
        KnownHashesResponse response = new KnownHashesResponse();
        TkmCard firstCard = cardRepository.findTopByOrderByIdAsc();
        if (firstCard != null && firstCard.getId() > hpanOffset + maxRecords) {
            log.info("First card has id " + firstCard.getId() + ", returning empty hpan set");
            response.setHpans(new HashSet<>());
            response.setNextHpanOffset(firstCard.getId() - 1);
        } else {
            List<TkmCard> cards = cardRepository.findByIdGreaterThanAndIdLessThanEqual(hpanOffset, hpanOffset + maxRecords);
            log.info("Found " + CollectionUtils.size(cards) + " hpans");
            response.setHpans(cards.stream().map(TkmCard::getHpan).collect(Collectors.toSet()));
            response.setNextHpanOffset(cards.stream().mapToLong(TkmCard::getId).max().orElse(0));
        }
        long diff = maxRecords - response.getHpans().size();
        if (diff > 0) {
            TkmCardToken firstToken = cardTokenRepository.findTopByOrderByIdAsc();
            if (firstToken != null && firstToken.getId() > htokenOffset + diff) {
                log.info("First token has id " + firstToken.getId() + ", returning empty htoken set");
                response.setHtokens(new HashSet<>());
                response.setNextHtokenOffset(firstToken.getId() - 1);
            } else {
                List<TkmCardToken> tokens = cardTokenRepository.findByIdGreaterThanAndIdLessThanEqual(htokenOffset, htokenOffset + diff);
                log.info("Found " + CollectionUtils.size(tokens) + " tokens");
                response.setHtokens(tokens.stream().map(TkmCardToken::getHtoken).collect(Collectors.toSet()));
                response.setNextHtokenOffset(tokens.stream().mapToLong(TkmCardToken::getId).max().orElse(0));
            }
        }
        log.trace(response);
        return response;
    }

}
