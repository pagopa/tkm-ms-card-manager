package it.gov.pagopa.tkm.ms.cardmanager.service;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentResponse;

public interface CircuitBreakerManager {

     String callRtdForHash(String toHash, String apimRtdSubscriptionKey);
     String getRtdForHashFallback(String toHash, String apimRtdSubscriptionKey, Throwable t );

     void consentClientGetConsent(String taxCode, TkmCard card);
     void consentClientGetConsentFallback(String taxCode, TkmCard card, Throwable t) throws Exception;
}
