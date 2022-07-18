package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.WalletsHashingEvaluationInput;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.exception.KafkaProcessMessageException;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentEntityEnum;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentResponse;
import it.gov.pagopa.tkm.ms.cardmanager.service.CircuitBreakerManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.CALL_TO_CONSENT_MANAGER_FAILED;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.CALL_TO_RTD_FAILED;

@Service
@Log4j2
public class CircuitBreakerManagerImpl implements CircuitBreakerManager {

    @Value("${keyvault.ocpApimSubscriptionKeyTkm}")
    private String apimSubscriptionKey;

    @CircuitBreaker(name = "rtdForHashCircuitBreaker", fallbackMethod = "getRtdForHashFallback")
    public String callRtdForHash(RtdHashingClient rtdHashingClient, String toHash, String apimRtdSubscriptionKey) {
        log.trace("Calling RTD for hash of " + toHash);
        try {
            return rtdHashingClient.getHash(new WalletsHashingEvaluationInput(toHash), apimRtdSubscriptionKey).getHashPan();
        } catch (Exception e) {
            log.error(e);
            throw new KafkaProcessMessageException(CALL_TO_RTD_FAILED);
        }
    }

    public String getRtdForHashFallback(RtdHashingClient rtdHashingClient, String toHash, String apimRtdSubscriptionKey, Throwable t) {
        log.info("RTD Hash fallback for hash value %s- cause {} " + t.getMessage());
        return "RTD Hash Error";
    }

    @CircuitBreaker(name = "consentClientGetConsentCircuitBreaker", fallbackMethod = "consentClientGetConsentFallback")
    public ConsentResponse consentClientGetConsent(ConsentClient consentClient, String taxCode, String hpan) {
        try {
            return consentClient.getConsent(taxCode, hpan, null, apimSubscriptionKey);
        } catch (FeignException fe) {
            if (fe.status() == HttpStatus.NOT_FOUND.value()) {
                log.info("Consent not found for card");
                return ConsentResponse.builder().consent(ConsentEntityEnum.Deny).build();
            }
            log.error(fe);
            throw new CardException(CALL_TO_CONSENT_MANAGER_FAILED);
        }
    }

    public ConsentResponse consentClientGetConsentFallback(ConsentClient consentClient, String taxCode, String hpan, Throwable t) {
        log.info("consent Client Get Consent Fallback %s- cause {} " + t.getMessage());
        if (t instanceof FeignException) {
            FeignException e = (FeignException) t;
            if (e.status() == HttpStatus.NOT_FOUND.value()) {
                log.info("Consent not found for card");
                return new ConsentResponse();
            } else {
                throw new CardException(CALL_TO_CONSENT_MANAGER_FAILED);
            }
        } else {
            throw new KafkaProcessMessageException(CALL_TO_CONSENT_MANAGER_FAILED);
        }
    }

}
