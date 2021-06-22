package it.gov.pagopa.tkm.ms.cardmanager.controller.impl;

import it.gov.pagopa.tkm.ms.cardmanager.controller.KnownHashesController;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class KnownHashesControllerImpl implements KnownHashesController {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardTokenRepository cardTokenRepository;

    @Override
    public KnownHashesResponse getKnownHashes(Integer maxRecords, Integer hpanOffset, Integer htokenOffset) {
        KnownHashesResponse response = new KnownHashesResponse();
        List<TkmCard> cards = cardRepository.findByHpanIsNotNull(new OffsetBasedPageRequest(maxRecords, hpanOffset));
        response.setHpans(cards.stream().map(TkmCard::getHpan).collect(Collectors.toSet()));
        int diff = maxRecords - response.getHpans().size();
        if (diff > 0) {
            List<TkmCardToken> tokens = cardTokenRepository.findByHtokenIsNotNull(new OffsetBasedPageRequest(diff, htokenOffset));
            response.setHtokens(tokens.stream().map(TkmCardToken::getHtoken).collect(Collectors.toSet()));
        }
        return response;
    }

}
