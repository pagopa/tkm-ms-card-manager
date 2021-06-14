package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTokenRepository extends JpaRepository<TkmCardToken, Long> {

    Page<TkmCardToken> findByHtokenIsNotNull(Pageable pageRequest);
}
