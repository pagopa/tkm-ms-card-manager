package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface CitizenCardRepository extends JpaRepository<TkmCitizenCard, Long> {

    List<TkmCitizenCard> findByCardId(Long cardId);

}
