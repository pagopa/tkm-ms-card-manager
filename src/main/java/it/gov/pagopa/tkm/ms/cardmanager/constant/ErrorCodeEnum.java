package it.gov.pagopa.tkm.ms.cardmanager.constant;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCodeEnum {

    MESSAGE_VALIDATION_FAILED("T1000", "Message validation failed, check for errors in the message fields"),
    MESSAGE_DECRYPTION_FAILED("T1001", "Message PGP decryption failed");

    @Getter
    private final String errorCode;

    @Getter
    private final String description;

}
