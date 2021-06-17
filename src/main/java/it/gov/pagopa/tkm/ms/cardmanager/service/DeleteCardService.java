package it.gov.pagopa.tkm.ms.cardmanager.service;

import it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete.DeleteQueueMessage;
import org.springframework.transaction.annotation.Transactional;


public interface DeleteCardService {
    @Transactional
    void deleteCard(DeleteQueueMessage deleteQueueMessage);
}
