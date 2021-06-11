package it.gov.pagopa.tkm.ms.cardmanager.controller.impl;

import it.gov.pagopa.tkm.ms.cardmanager.controller.KnownHashController;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.stream.Collectors;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams.TOTAL_NUMBER_PAGES_HEADER;

@RestController
public class KnownHashControllerImpl implements KnownHashController {
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardTokenRepository cardTokenRepository;

    @Override
    public Set<String> getKnownHashpanSet(Integer maxRecords, Integer pageNumber, HttpServletResponse response) {
        Page<TkmCard> cards = cardRepository.findByHpanIsNotNull(PageRequest.of(pageNumber, maxRecords, Sort.by("id")));
        response.addHeader(TOTAL_NUMBER_PAGES_HEADER, String.valueOf(cards.getTotalPages()));
        return cards.get().map(TkmCard::getHpan).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getKnownHashTokenSet(Integer maxRecords, Integer pageNumber, HttpServletResponse response) {
        Page<TkmCardToken> cards = cardTokenRepository.findByHtokenIsNotNull(PageRequest.of(pageNumber, maxRecords, Sort.by("id")));
        response.addHeader(TOTAL_NUMBER_PAGES_HEADER, String.valueOf(cards.getTotalPages()));
        return cards.get().map(TkmCardToken::getHtoken).collect(Collectors.toSet());
    }
}
