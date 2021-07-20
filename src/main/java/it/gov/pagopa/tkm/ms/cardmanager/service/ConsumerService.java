package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.*;

import java.util.*;
import java.util.concurrent.*;

public interface ConsumerService {

    void consume(List<String> messages) throws ExecutionException, InterruptedException, JsonProcessingException, Exception;

    void consumeDelete(String message);

}
