package it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager;

import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import org.springframework.cloud.openfeign.*;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams.*;

@FeignClient(value = "consent-card", url = "${client-urls.consent-manager}")
public interface ConsentClient {

    @CircuitBreaker(name = "consentClientCircuitBreaker", fallbackMethod = "geConsentFallback")
    @GetMapping("/consent")
    ConsentResponse getConsent(
            @RequestHeader(TAX_CODE_HEADER) String taxCode,
            @RequestParam(value = HPAN_QUERY_PARAM, required = false) String hpan,
            @RequestParam(value = SERVICES_QUERY_PARAM, required = false) String[] services
    );

    public String getParFallback(String accountNumber, Throwable t ){
        log.info(String.format("MASTERCARD fallback for get par of account number %s- cause {}", accountNumber), t.toString());
        return "MASTERCARD fallback for get par. Some error occurred while calling get Par for Mastercard client";
    }


}
