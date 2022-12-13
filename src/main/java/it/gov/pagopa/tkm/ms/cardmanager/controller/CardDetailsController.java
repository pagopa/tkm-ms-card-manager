package it.gov.pagopa.tkm.ms.cardmanager.controller;

import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiEndpoints.BASE_PATH_CARDS;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams.HPAN_HEADER;

@RequestMapping(BASE_PATH_CARDS)
public interface CardDetailsController {

    @GetMapping
    ResponseEntity<CardDetailsResponse> getCardDetails(@RequestHeader(name = HPAN_HEADER) String hpan);

}
