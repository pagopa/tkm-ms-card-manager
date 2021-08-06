package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.WalletsHashingEvaluationInput;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.exception.KafkaProcessMessageException;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentResponse;
import it.gov.pagopa.tkm.ms.cardmanager.service.CircuitBreakerManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.CALL_TO_CONSENT_MANAGER_FAILED;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.CALL_TO_RTD_FAILED;

@Service
@Log4j2
public class CircuitBreakerManagerImpl implements CircuitBreakerManager {

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
        log.info("RTD Hash fallback for hash value%s- cause {} " + t.getMessage());
        return "RTD Hash Error";
    }

    @CircuitBreaker(name = "consentClientGetConsentCircuitBreaker", fallbackMethod = "consentClientGetConsentFallback")
    public ConsentResponse consentClientGetConsent(ConsentClient consentClient, String taxCode, TkmCard card) {
        return consentClient.getConsent(taxCode, card.getHpan(), null);
    }

    public void consentClientGetConsentFallback(ConsentClient consentClient, String taxCode, TkmCard card, Throwable t){
        log.info("consent Client Get Consent Fallback%s- cause {} " + t.getMessage());
        throw new CardException(CALL_TO_CONSENT_MANAGER_FAILED);
    }

}
