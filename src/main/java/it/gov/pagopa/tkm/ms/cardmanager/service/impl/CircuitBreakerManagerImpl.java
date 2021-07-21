package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.WalletsHashingEvaluationInput;
import it.gov.pagopa.tkm.ms.cardmanager.exception.KafkaProcessMessageException;
import it.gov.pagopa.tkm.ms.cardmanager.service.CircuitBreakerManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.CALL_TO_RTD_FAILED;

@Service
@Log4j2
public class CircuitBreakerManagerImpl implements CircuitBreakerManager {

    @Autowired
    private RtdHashingClient rtdHashingClient;

    @CircuitBreaker(name = "rtdForHashCircuitBreaker", fallbackMethod = "getRtdForHashFallback")
    public String callRtdForHash(String toHash, String apimRtdSubscriptionKey) {
        log.trace("Calling RTD for hash of " + toHash);
        try {
            return rtdHashingClient.getHash(new WalletsHashingEvaluationInput(toHash), apimRtdSubscriptionKey).getHashPan();
        } catch (Exception e) {
            log.error(e);
            throw new KafkaProcessMessageException(CALL_TO_RTD_FAILED);
        }
    }


    public String getRtdForHashFallback(String toHash, String apimRtdSubscriptionKey, Throwable t ){
        log.info("RTD Hash fallback for hash value%s- cause {} "+  t.getMessage());
        return "RTD Hash Error";
    }

}
