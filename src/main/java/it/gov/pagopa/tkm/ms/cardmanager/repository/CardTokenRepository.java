package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CardTokenRepository extends JpaRepository<TkmCardToken, Long> {

    List<TkmCardToken> findByHtokenIsNotNull(Pageable pageRequest);
}
