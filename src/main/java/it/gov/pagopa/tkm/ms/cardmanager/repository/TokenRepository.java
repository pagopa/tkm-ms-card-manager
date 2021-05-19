package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import org.springframework.data.jpa.repository.*;

public interface TokenRepository extends JpaRepository<TkmCardToken, Long> {

}
