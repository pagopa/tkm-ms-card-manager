package it.gov.pagopa.tkm.ms.cardmanager.controller.impl;

import it.gov.pagopa.tkm.annotation.EnableExecutionTime;
import it.gov.pagopa.tkm.annotation.EnableStartEndLogging;
import it.gov.pagopa.tkm.ms.cardmanager.controller.QueryServiceMultiThreading;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardSubSet;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardTokenSubSet;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.Future;

@Service
public class QueryServiceMultiThreadingImpl implements QueryServiceMultiThreading {

    @Autowired
    private CardTokenRepository cardTokenRepository;

    @Autowired
    private CardRepository cardRepository;

    @Async
    @EnableExecutionTime
    @EnableStartEndLogging
    @Transactional(readOnly = true)
    public Future<List<TkmCardTokenSubSet>> getTkmCardTokenSubSetAsync(long min, long max) {
        return new AsyncResult<>(cardTokenRepository.findByIdGreaterThanEqualAndIdLessThan(min, max));
    }

    @Async
    @EnableExecutionTime
    @Transactional(readOnly = true)
    public Future<List<TkmCardSubSet>> getTkmCardSubSetAsync(long min, long max) {
        return new AsyncResult<>(cardRepository.findByIdGreaterThanEqualAndIdLessThanAndHpanIsNotNull(min, max));
    }
}
