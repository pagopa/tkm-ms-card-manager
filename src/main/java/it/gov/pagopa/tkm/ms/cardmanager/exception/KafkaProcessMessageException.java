package it.gov.pagopa.tkm.ms.cardmanager.exception;

import it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class KafkaProcessMessageException extends RuntimeException {

    private final ErrorCodeEnum errorCode;

    public KafkaProcessMessageException(ErrorCodeEnum ec) {
        super(ec.getStatusCode() + " - " + ec.getMessage());
        errorCode = ec;
    }

}
