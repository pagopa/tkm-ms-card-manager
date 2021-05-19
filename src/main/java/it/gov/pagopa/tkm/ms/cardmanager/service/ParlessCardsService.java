package it.gov.pagopa.tkm.ms.cardmanager.service;

import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;

import java.util.*;

public interface ParlessCardsService {

    List<ParlessCardResponse> getParlessCards(Integer maxRecords);

}
