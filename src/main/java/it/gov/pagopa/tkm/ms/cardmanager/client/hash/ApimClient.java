package it.gov.pagopa.tkm.ms.cardmanager.client.hash;

import it.gov.pagopa.tkm.ms.cardmanager.client.hash.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.hash.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import org.springframework.cloud.openfeign.*;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "hash", url = "${client-urls.rtd-hashing-url}")
public interface ApimClient {

    @GetMapping
    WalletsHashingEvaluation getHash(
            @RequestBody WalletsHashingEvaluationInput walletsHashingEvaluationInput,
            @RequestHeader(ApiParams.OCP_APIM_SUBSCRIPTION_KEY_HEADER) String subscriptionHeader
    );

}
