package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;
import org.bouncycastle.openpgp.*;

public interface ProducerService {

    void sendMessage(WriteQueue writeQueue) throws PGPException, JsonProcessingException;

}
