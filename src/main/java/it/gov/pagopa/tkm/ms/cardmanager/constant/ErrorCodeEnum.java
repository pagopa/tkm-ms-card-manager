package it.gov.pagopa.tkm.ms.cardmanager.constant;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCodeEnum {

    PAN_MISSING("T1000", "Pan and par missing from queue message"),
    REQUEST_VALIDATION_FAILED("T1001", "Request validation failed, check for errors in the request body or headers");

    @Getter
    private final String errorCode;

    @Getter
    private final String description;

}
