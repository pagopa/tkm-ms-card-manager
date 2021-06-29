package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import org.springframework.data.jpa.repository.*;

public interface CitizenRepository extends JpaRepository<TkmCitizen, Long> {

    TkmCitizen findByTaxCode(String taxCode);

}
