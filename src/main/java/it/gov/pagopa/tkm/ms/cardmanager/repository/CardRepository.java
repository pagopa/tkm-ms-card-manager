package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.*;
import java.util.List;

public interface CardRepository extends JpaRepository<TkmCard, Long> {

    TkmCard findByHpan(String hpan);

    TkmCard findByPar(String par);

    TkmCard findByHpanAndPar(String hpan, String par);

    List<TkmCard> findByIdGreaterThanAndIdLessThanEqual(Long min, Long max);

    @Cacheable(value = "first-card", unless = "#result == null")
    TkmCard findTopByOrderByIdAsc();

    TkmCard findByHpanAndDeletedFalse(String hpan);

    TkmCard findByParAndDeletedFalse(String par);

    List<TkmCard> findByParIsNullAndLastReadDateBeforeOrParIsNullAndLastReadDateIsNull(Instant oneDayAgo, Pageable pageable);

}
