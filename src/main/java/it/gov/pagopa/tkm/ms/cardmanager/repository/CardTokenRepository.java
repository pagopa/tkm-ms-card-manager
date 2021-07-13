package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardTokenSubSet;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardTokenRepository extends JpaRepository<TkmCardToken, Long> {

    TkmCardToken findByHtokenAndDeletedFalse(String htoken);

    List<TkmCardTokenSubSet> findByIdGreaterThanEqualAndIdLessThan(Long min, Long max);

    @Cacheable(value = "first-token", unless = "#result == null")
    TkmCardToken findTopByOrderByIdAsc();
}
