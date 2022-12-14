package it.gov.pagopa.tkm.ms.cardmanager.controller.impl;

import it.gov.pagopa.tkm.ms.cardmanager.controller.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.*;
import it.gov.pagopa.tkm.util.*;
import lombok.extern.log4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.stream.*;

@Log4j2
@RestController
public class CardDetailsControllerImpl implements CardDetailsController {

    @Autowired
    private CardRepository cardRepository;

    @Override
    public ResponseEntity<CardDetailsResponse> getCardDetails(String hpan) {
        log.info("Getting card with hpan " + ObfuscationUtils.obfuscateHpan(hpan));
        TkmCard card = cardRepository.findByHpan(hpan);
        if (card == null) {
            log.warn("Card not found");
            return ResponseEntity.notFound().build();
        }
        log.info("Card found, id: " + card.getId());
        return ResponseEntity.ok(new CardDetailsResponse(card.getHpan(), card.getPar(), card.getTokens().stream().map(TkmCardToken::getHtoken).collect(Collectors.toSet())));
    }

}
