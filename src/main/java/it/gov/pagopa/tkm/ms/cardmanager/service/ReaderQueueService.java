package it.gov.pagopa.tkm.ms.cardmanager.service;

import java.util.concurrent.Future;

public interface ReaderQueueService {
    Future<Void> workOnMessage(String message);
}
