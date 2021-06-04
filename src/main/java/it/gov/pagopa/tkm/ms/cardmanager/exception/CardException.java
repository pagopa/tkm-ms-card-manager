package it.gov.pagopa.tkm.ms.cardmanager.exception;

import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
public class CardException extends RuntimeException {

    private ErrorCodeEnum errorCode;

    public CardException(ErrorCodeEnum ec) {
        super(ec.getErrorCode() + " - " + ec.getDescription());
        errorCode = ec;
    }

}
