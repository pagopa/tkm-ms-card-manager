package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardSubSet;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CardRepository extends JpaRepository<TkmCard, Long> {

    TkmCard findByHpan(String hpan);

    TkmCard findByPar(String par);

    TkmCard findByHpanAndPar(String hpan, String par);

    List<TkmCardSubSet> findByIdGreaterThanEqualAndIdLessThanAndHpanIsNotNull(Long min, Long max);

    @Cacheable(value = "first-card", unless = "#result == null")
    TkmCard findTopByOrderByIdAsc();

    List<TkmCard> findByParIsNullAndLastReadDateBeforeAndCircuitNotOrParIsNullAndLastReadDateIsNullAndCircuitNot(Instant oneDayAgo, CircuitEnum circuit1, CircuitEnum circuit2, Pageable pageable);


}
