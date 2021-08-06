package it.gov.pagopa.tkm.ms.cardmanager.service;

import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.RtdHashingClient;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.WalletsHashingEvaluationInput;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.response.WalletsHashingEvaluation;
import it.gov.pagopa.tkm.ms.cardmanager.client.internal.consentmanager.ConsentClient;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
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
    void callHashSuccess() {
        String hash = "hash";
        Mockito.when(rtdHashingClient.getHash(Mockito.any(WalletsHashingEvaluationInput.class), Mockito.anyString())).thenReturn(new WalletsHashingEvaluation(hash, "salt"));
        String hashMethod = circuitBreakerManager.callRtdForHash(rtdHashingClient, "toHash", "key");
        Assertions.assertEquals(hash, hashMethod);
    }

    @Test
    void callConsentSuccess() {
        ConsentResponse consentResponse = ConsentResponse.builder().taxCode("taxCode").consent(ConsentEntityEnum.Allow).build();
        Mockito.when(consentClient.getConsent(Mockito.anyString(),Mockito.anyString(),Mockito.any())).thenReturn(consentResponse);
                ConsentResponse consentResponseCircuit = circuitBreakerManager.consentClientGetConsent(consentClient, "taxCode", TkmCard.builder().hpan("hash").build());
        Assertions.assertEquals(consentResponse, consentResponseCircuit);
    }
}