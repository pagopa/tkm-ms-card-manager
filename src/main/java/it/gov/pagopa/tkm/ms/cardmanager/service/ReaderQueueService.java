package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bouncycastle.openpgp.PGPException;

import java.util.concurrent.Future;

public interface ReaderQueueService {
    Future<Void> workOnMessage(String message) throws JsonProcessingException;
}
