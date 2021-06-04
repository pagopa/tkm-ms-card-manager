package it.gov.pagopa.tkm.ms.cardmanager.controller.impl;

import it.gov.pagopa.tkm.ms.cardmanager.controller.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class ConsentUpdateControllerImpl implements ConsentUpdateController {

    @Autowired
    private ConsentUpdateServiceImpl consentUpdateService;

    @Override
    public void updateConsent(ConsentResponse consentResponse) {
        consentUpdateService.updateConsent(consentResponse);
    }

}
