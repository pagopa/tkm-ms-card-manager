package it.gov.pagopa.tkm.ms.cardmanager.controller;

import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams.*;

public interface ParlessCardsController {

    @Transactional
    @GetMapping
    List<ParlessCardResponse> getParlessCards(
            @RequestParam(MAX_NUMBER_OF_CARDS) Integer maxRecords
    );

}
