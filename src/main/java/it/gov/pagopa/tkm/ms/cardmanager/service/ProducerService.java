package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;

public interface ProducerService {

    void sendMessage(WriteQueue writeQueue) throws JsonProcessingException;

}
