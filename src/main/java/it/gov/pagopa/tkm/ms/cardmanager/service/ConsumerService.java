package it.gov.pagopa.tkm.ms.cardmanager.service;

import org.springframework.transaction.annotation.*;

public interface ConsumerService {

    @Transactional
    void consume(String message, String fromIssuer) throws Exception;

    void consumeDelete(String message);

}
