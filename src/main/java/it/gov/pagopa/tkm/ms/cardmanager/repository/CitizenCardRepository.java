package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface CitizenCardRepository extends JpaRepository<TkmCitizenCard, Long> {

    List<TkmCitizenCard> findByCardId(Long cardId);

    List<TkmCitizenCard> findByCardIdIn(List<Long> cardId);

    TkmCitizenCard findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(String taxCode, String hpan);

    TkmCitizenCard findByDeletedFalseAndCitizen_TaxCodeAndCard_Par(String taxCode, String par);

}
