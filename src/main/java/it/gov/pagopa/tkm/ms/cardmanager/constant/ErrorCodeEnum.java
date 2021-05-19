package it.gov.pagopa.tkm.ms.cardmanager.constant;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCodeEnum {

    PAN_NOT_FOUND("T1000", "Pan, par and hpan missing from queue message");

    @Getter
    private final String errorCode;

    @Getter
    private final String description;

}
