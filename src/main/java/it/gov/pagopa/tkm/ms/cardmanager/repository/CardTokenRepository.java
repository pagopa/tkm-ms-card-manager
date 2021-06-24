package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import org.springframework.cache.annotation.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CardTokenRepository extends JpaRepository<TkmCardToken, Long> {

    List<TkmCardToken> findByIdGreaterThanAndIdLessThanEqual(Long min, Long max);

    @Cacheable(value = "first-token", unless = "#result == null")
    TkmCardToken findTopByOrderByIdAsc();

}
