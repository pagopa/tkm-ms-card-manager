package it.gov.pagopa.tkm.ms.cardmanager.constant;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCodeEnum {

    REQUEST_VALIDATION_FAILED(2000, "Request validation failed, check for errors in the request"),
    MESSAGE_VALIDATION_FAILED(2001, "Message validation failed, check for errors in the message fields"),
    MESSAGE_DECRYPTION_FAILED(2002, "Message PGP decryption failed");

    @Getter
    private final Integer statusCode;

    @Getter
    private final String message;

}
