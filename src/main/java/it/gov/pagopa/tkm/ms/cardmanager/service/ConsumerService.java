package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bouncycastle.openpgp.PGPException;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ConsumerService {

    void consume(List<String> messages) throws ExecutionException, InterruptedException, JsonProcessingException, PGPException;

    void consumeDelete(String message);

}
