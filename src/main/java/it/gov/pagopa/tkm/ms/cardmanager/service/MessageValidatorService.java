package it.gov.pagopa.tkm.ms.cardmanager.service;

import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;

public interface MessageValidatorService {
    <T> void validateMessage(T message) throws CardException;
}
