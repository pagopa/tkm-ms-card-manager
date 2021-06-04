package it.gov.pagopa.tkm.ms.cardmanager.controller;

import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiEndpoints.BASE_PATH_CONSENT_UPDATE;

@RequestMapping(BASE_PATH_CONSENT_UPDATE)
public interface ConsentUpdateController {

    @Transactional
    @PutMapping
    @ResponseStatus(value = HttpStatus.OK)
    void updateConsent(@RequestBody ConsentResponse consentResponse);

}
