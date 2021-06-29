package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCitizenCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TkmCitizenCardRepository extends JpaRepository<TkmCitizenCard, Long> {
    TkmCitizenCard findByDeletedFalseAndCitizen_TaxCodeAndCard_Hpan(String taxCode, String hpan);
}
