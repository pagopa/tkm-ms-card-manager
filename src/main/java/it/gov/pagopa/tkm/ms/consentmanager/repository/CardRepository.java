package it.gov.pagopa.tkm.ms.consentmanager.repository;

import it.gov.pagopa.tkm.ms.consentmanager.model.entity.*;
import org.springframework.data.jpa.repository.*;

public interface CardRepository extends JpaRepository<TkmCard, Long> {

}
