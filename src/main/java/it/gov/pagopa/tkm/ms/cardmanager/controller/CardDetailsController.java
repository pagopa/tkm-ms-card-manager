package it.gov.pagopa.tkm.ms.cardmanager.controller;

import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;

import javax.validation.*;
import javax.validation.constraints.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiEndpoints.BASE_PATH_CARDS;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams.HPAN_HEADER;

@RequestMapping(BASE_PATH_CARDS)
@Validated
public interface CardDetailsController {

    @Transactional
    @GetMapping
    ResponseEntity<CardDetailsResponse> getCardDetails(@RequestHeader(name = HPAN_HEADER) @Valid @Size(min = 64, max = 64) String hpan);

}
