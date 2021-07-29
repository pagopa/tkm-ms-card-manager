package it.gov.pagopa.tkm.ms.cardmanager.service;

import java.util.*;

public interface ConsumerService {

    void consume(List<String> messages) throws Exception;

    void consumeDelete(String message);

}
