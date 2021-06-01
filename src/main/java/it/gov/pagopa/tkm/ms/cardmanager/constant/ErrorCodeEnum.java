package it.gov.pagopa.tkm.ms.cardmanager.constant;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCodeEnum {

    REQUEST_VALIDATION_FAILED("T1000", "Request validation failed, check for errors in the request"),
    MESSAGE_VALIDATION_FAILED("T1001", "Message validation failed, check for errors in the message fields"),
    MESSAGE_DECRYPTION_FAILED("T1002", "Message PGP decryption failed"),
    MESSAGE_ENCRYPTION_FAILED("T1003", "Message PGP encryption failed");

    @Getter
    private final String errorCode;

    @Getter
    private final String description;

}
