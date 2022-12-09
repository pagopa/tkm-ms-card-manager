package it.gov.pagopa.tkm.ms.cardmanager.controller;

import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import org.springframework.web.bind.annotation.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiEndpoints.BASE_PATH_HPAN;

@RequestMapping(BASE_PATH_HPAN)
public interface CardDetailsController {

    @GetMapping
    CardDetailsResponse getCardDetails(@RequestHeader(name = "hpan") String hpan);

}
