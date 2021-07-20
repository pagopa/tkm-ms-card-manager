package it.gov.pagopa.tkm.ms.cardmanager.exception;

import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
public class CardException extends RuntimeException {

    private final ErrorCodeEnum errorCode;

    public CardException(ErrorCodeEnum ec) {
        super(ec.getStatusCode() + " - " + ec.getMessage());
        errorCode = ec;
    }

}
