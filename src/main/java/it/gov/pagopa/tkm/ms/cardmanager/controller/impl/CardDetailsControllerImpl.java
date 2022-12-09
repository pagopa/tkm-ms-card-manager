package it.gov.pagopa.tkm.ms.cardmanager.controller.impl;

import it.gov.pagopa.tkm.ms.cardmanager.controller.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.stream.*;

@RestController
public class CardDetailsControllerImpl implements CardDetailsController {

    @Autowired
    private CardRepository cardRepository;

    @Override
    public CardDetailsResponse getCardDetails(String hpan) {
        TkmCard card = cardRepository.findByHpan(hpan);
        return new CardDetailsResponse(card.getHpan(), card.getPar(), card.getTokens().stream().map(TkmCardToken::getHtoken).collect(Collectors.toSet()));
    }

}
