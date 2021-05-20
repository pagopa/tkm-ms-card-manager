package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.*;
import java.util.*;

public interface CardRepository extends JpaRepository<TkmCard, Long> {

    TkmCard findByTaxCodeAndHpanAndDeletedFalse(String taxCode, String hpan);

    TkmCard findByTaxCodeAndParAndDeletedFalse(String taxCode, String par);

    List<TkmCard> findByParIsNullAndDeletedFalseAndLastReadDateBeforeOrParIsNullAndDeletedFalseAndLastReadDateIsNull(Instant oneDayAgo, Pageable pageable);

}
