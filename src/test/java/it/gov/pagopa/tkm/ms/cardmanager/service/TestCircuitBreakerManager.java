package it.gov.pagopa.tkm.ms.cardmanager.service;

import feign.FeignException;
import feign.Request;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.WalletsHashingEvaluationInput;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.response.WalletsHashingEvaluation;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.exception.KafkaProcessMessageException;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentEntityEnum;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentResponse;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.CircuitBreakerManagerImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.CALL_TO_CONSENT_MANAGER_FAILED;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.CALL_TO_RTD_FAILED;
import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestCircuitBreakerManager {
    @InjectMocks
    private CircuitBreakerManagerImpl circuitBreakerManager;

    @Mock
    private RtdHashingClient rtdHashingClient;

    @Mock
    private ConsentClient consentClient;

    @Test
    void callHash_success() {
        String hash = "hash";
        Mockito.when(rtdHashingClient.getHash(Mockito.any(WalletsHashingEvaluationInput.class), Mockito.anyString())).thenReturn(new WalletsHashingEvaluation(hash, "salt"));
        String hashMethod = circuitBreakerManager.callRtdForHash(rtdHashingClient, "toHash", "key");
        Assertions.assertEquals(hash, hashMethod);
    }

    @Test
    void callHash_error() {
        Mockito.when(rtdHashingClient.getHash(Mockito.any(WalletsHashingEvaluationInput.class), Mockito.anyString())).thenThrow(FeignException.class);
        KafkaProcessMessageException messageException = Assertions.assertThrows(KafkaProcessMessageException.class, () -> circuitBreakerManager.callRtdForHash(rtdHashingClient, "toHash", "key"));
        Assertions.assertEquals(CALL_TO_RTD_FAILED, messageException.getErrorCode());
    }

    @Test
    void callConsent_success() {
        ConsentResponse consentResponse = ConsentResponse.builder().taxCode("taxCode").consent(ConsentEntityEnum.Allow).build();
        Mockito.when(consentClient.getConsent(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyString())).thenReturn(consentResponse);
        ConsentResponse consentResponseCircuit = circuitBreakerManager.consentClientGetConsent(consentClient, "taxCode", "hash");
        Assertions.assertEquals(consentResponse, consentResponseCircuit);
    }

    @Test
    void callConsent_exceptionFeign() {
        Mockito.when(consentClient.getConsent(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyString())).thenThrow(FeignException.class);
        CardException cardException = Assertions.assertThrows(CardException.class, () -> circuitBreakerManager.consentClientGetConsent(consentClient, "taxCode", "hash"));
        Assertions.assertEquals(CALL_TO_CONSENT_MANAGER_FAILED, cardException.getErrorCode());
    }

    @Test
    void callConsent_404() {
        FeignException.NotFound notFound = new FeignException.NotFound("", mock(Request.class), null);
        Mockito.when(consentClient.getConsent(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyString())).thenThrow(notFound);
        ConsentResponse consentResponseCircuit = circuitBreakerManager.consentClientGetConsent(consentClient, "taxCode", "hash");
        Assertions.assertEquals(ConsentResponse.builder().consent(ConsentEntityEnum.Deny).build(), consentResponseCircuit);
    }
}