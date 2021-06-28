package it.gov.pagopa.tkm.ms.cardmanager.service;

import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.*;

public interface CardService {

    void updateOrCreateCard(ReadQueue readQueue, boolean fromIssuer);

}
