package it.gov.pagopa.tkm.ms.cardmanager.controller.impl;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardSubSet;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardTokenSubSet;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestQueryServiceMultiThreadingImpl {
    @InjectMocks
    private QueryServiceMultiThreadingImpl queryServiceMultiThreading;

    @Mock
    private CardTokenRepository cardTokenRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TkmCardTokenSubSet tkmCardTokenSubSet;

    @Mock
    private TkmCardSubSet tkmCardSubSet;

    @Test
    void getTkmCardTokenSubSetAsync() throws ExecutionException, InterruptedException {
        List<TkmCardTokenSubSet> value = Collections.singletonList(tkmCardTokenSubSet);
        Mockito.when(cardTokenRepository.findByIdGreaterThanEqualAndIdLessThan(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(value);
        Future<List<TkmCardTokenSubSet>> tkmCardTokenSubSetAsync = queryServiceMultiThreading.getTkmCardTokenSubSetAsync(0, 10);
        Assertions.assertEquals(value, tkmCardTokenSubSetAsync.get());
    }

    @Test
    void getTkmCardSubSetAsync() throws ExecutionException, InterruptedException {
        List<TkmCardSubSet> value = Collections.singletonList(tkmCardSubSet);
        Mockito.when(cardRepository.findByIdGreaterThanEqualAndIdLessThanAndHpanIsNotNull(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(value);
        Future<List<TkmCardSubSet>> tkmCardSubSetAsync = queryServiceMultiThreading.getTkmCardSubSetAsync(0, 10);
        Assertions.assertEquals(value, tkmCardSubSetAsync.get());
    }
}