package it.gov.pagopa.tkm.ms.cardmanager.controller;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardSubSet;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardTokenSubSet;

import java.util.List;
import java.util.concurrent.Future;

public interface QueryServiceMultiThreading {
    Future<List<TkmCardTokenSubSet>> getTkmCardTokenSubSetAsync(long min, long max);

    Future<List<TkmCardSubSet>> getTkmCardSubSetAsync(long min, long max);
}
