package it.gov.pagopa.tkm.ms.cardmanager.repository;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CardRepository extends JpaRepository<TkmCard, Long> {

    TkmCard findByTaxCodeAndHpanAndDeletedFalse(String taxCode, String hpan);

    TkmCard findByTaxCodeAndParAndDeletedFalse(String taxCode, String par);

    List<TkmCard> findByTaxCodeAndParIsNotNullAndDeletedFalse(String taxCode);

    List<TkmCard> findByTaxCodeAndHpanInAndParIsNotNullAndDeletedFalse(String taxCode, List<String> hpan);

    List<TkmCard> findByParIsNullAndDeletedFalseAndLastReadDateBeforeOrParIsNullAndDeletedFalseAndLastReadDateIsNull(Instant oneDayAgo, Pageable pageable);

    List<TkmCard> findByIdGreaterThanAndIdLessThanEqual(Long min, Long max);

    @Cacheable(value = "first-card", unless = "#result == null")
    TkmCard findTopByOrderByIdAsc();

}
