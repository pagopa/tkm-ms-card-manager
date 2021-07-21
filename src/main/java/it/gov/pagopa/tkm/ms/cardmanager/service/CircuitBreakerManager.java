package it.gov.pagopa.tkm.ms.cardmanager.service;

public interface CircuitBreakerManager {

     String callRtdForHash(String toHash, String apimRtdSubscriptionKey);
     String getRtdForHashFallback(String toHash, String apimRtdSubscriptionKey, Throwable t );
}
