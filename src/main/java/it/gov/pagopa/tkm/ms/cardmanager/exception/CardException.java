package it.gov.pagopa.tkm.ms.cardmanager.exception;

import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class CardException extends RuntimeException {

    public CardException(){ }
    private ErrorCodeEnum errorCode;

    public CardException(ErrorCodeEnum errorCode) {
        super(errorCode.getErrorCode() + " - " + errorCode.getDescription());
        this.setErrorCode(errorCode);
    }

}
