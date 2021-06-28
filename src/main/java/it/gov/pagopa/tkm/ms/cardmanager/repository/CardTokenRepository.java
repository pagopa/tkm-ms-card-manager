package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardTokenRepository extends JpaRepository<TkmCardToken, Long> {
    Optional<TkmCardToken> findByHtokenAndDeletedFalse(String htoken);
    List<TkmCardToken> findByIdGreaterThanAndIdLessThanEqual(Long min, Long max);

    @Cacheable(value = "first-token", unless = "#result == null")
    TkmCardToken findTopByOrderByIdAsc();
}
