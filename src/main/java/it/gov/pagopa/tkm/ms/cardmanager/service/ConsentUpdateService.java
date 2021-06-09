package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import org.springframework.transaction.annotation.*;

public interface ConsentUpdateService {

    @Transactional
    void updateConsent(ConsentResponse consentResponse) throws JsonProcessingException;

}
