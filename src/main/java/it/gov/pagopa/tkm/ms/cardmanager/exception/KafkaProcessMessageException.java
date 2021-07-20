package it.gov.pagopa.tkm.ms.cardmanager.exception;

import it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = false)
public class KafkaProcessMessageException extends RuntimeException {

    private final ErrorCodeEnum errorCode;
    @Getter
    @Setter
    private String msg;

    public KafkaProcessMessageException(ErrorCodeEnum ec, String msg) {
        super(ec.getStatusCode() + " - " + ec.getMessage());
        errorCode = ec;
        this.msg = msg;
    }

    public KafkaProcessMessageException(ErrorCodeEnum ec) {
        super(ec.getStatusCode() + " - " + ec.getMessage());
        errorCode = ec;
    }
}
