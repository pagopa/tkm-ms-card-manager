package it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd;

import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.external.rtd.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import org.springframework.cloud.openfeign.*;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "hash", url = "${client-urls.rtd-hashing-url}")
public interface RtdHashingClient {

    @GetMapping
    WalletsHashingEvaluation getHash(
            @RequestBody WalletsHashingEvaluationInput walletsHashingEvaluationInput,
            @RequestHeader(ApiParams.OCP_APIM_SUBSCRIPTION_KEY_HEADER) String subscriptionHeader
    );

}
