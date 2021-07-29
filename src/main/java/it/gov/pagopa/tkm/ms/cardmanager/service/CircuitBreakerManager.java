package it.gov.pagopa.tkm.ms.cardmanager.service;

import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;

public interface CircuitBreakerManager {

     String callRtdForHash(RtdHashingClient rtdHashingClient, String toHash, String apimRtdSubscriptionKey);
     String getRtdForHashFallback(RtdHashingClient rtdHashingClient, String toHash, String apimRtdSubscriptionKey, Throwable t );

     void consentClientGetConsent(ConsentClient consentClient, String taxCode, TkmCard card);
     void consentClientGetConsentFallback(ConsentClient consentClient, String taxCode, TkmCard card, Throwable t) throws Exception;
}
