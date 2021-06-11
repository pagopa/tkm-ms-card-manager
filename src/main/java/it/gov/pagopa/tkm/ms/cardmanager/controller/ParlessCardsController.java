package it.gov.pagopa.tkm.ms.cardmanager.controller;

import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiEndpoints.BASE_PATH_PARLESS_CARDS;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams.*;

@RequestMapping(BASE_PATH_PARLESS_CARDS)
public interface ParlessCardsController {

    @Transactional
    @GetMapping
    List<ParlessCardResponse> getParlessCards(
            @RequestParam(MAX_NUMBER_OF_CARDS_PARAM) Integer maxRecords
    );

}
