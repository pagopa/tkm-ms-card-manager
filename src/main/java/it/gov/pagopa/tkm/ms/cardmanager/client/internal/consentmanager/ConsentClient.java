package it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager;

import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import org.springframework.cloud.openfeign.*;
import org.springframework.web.bind.annotation.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams.*;

@FeignClient(value = "consent-card", url = "${client-urls.consent-manager}")
public interface ConsentClient {

    @GetMapping("/consent")
    ConsentResponse getConsent(
            @RequestHeader(TAX_CODE_HEADER) String taxCode,
            @RequestParam(value = HPAN_QUERY_PARAM, required = false) String hpan,
            @RequestParam(value = SERVICES_QUERY_PARAM, required = false) String[] services
    );

}
