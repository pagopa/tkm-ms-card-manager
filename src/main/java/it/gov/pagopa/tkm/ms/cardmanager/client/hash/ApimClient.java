package it.gov.pagopa.tkm.ms.cardmanager.client.hash;

import it.gov.pagopa.tkm.ms.cardmanager.client.hash.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.hash.model.response.*;
import org.springframework.cloud.openfeign.*;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "hash", url = "${apim-url}")
public interface ApimClient {

    @GetMapping("/v1/static-contents/wallets/hashing/actions/evaluate")
    WalletsHashingEvaluation getHash(@RequestBody WalletsHashingEvaluationInput walletsHashingEvaluationInput);

}
