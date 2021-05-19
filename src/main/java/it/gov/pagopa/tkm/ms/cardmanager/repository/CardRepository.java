package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import org.springframework.data.jpa.repository.*;

public interface CardRepository extends JpaRepository<TkmCard, Long> {

    TkmCard findByTaxCodeAndHpan(String taxCode, String hpan);

    TkmCard findByTaxCodeAndPar(String taxCode, String par);

}
