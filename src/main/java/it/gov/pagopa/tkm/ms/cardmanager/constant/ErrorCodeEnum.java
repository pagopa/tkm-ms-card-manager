package it.gov.pagopa.tkm.ms.cardmanager.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCodeEnum {

    REQUEST_VALIDATION_FAILED(2000, "Request validation failed, check for errors in the request"),
    MESSAGE_VALIDATION_FAILED(2001, "Message validation failed, check for errors in the message fields"),
    MESSAGE_DECRYPTION_FAILED(2002, "Message PGP decryption failed"),
    MESSAGE_WRITE_FAILED(2003, "Could not write message on queue"),
    CALL_TO_CONSENT_MANAGER_FAILED(2004, "Could not execute call to Consent Manager"),
    CALL_TO_RTD_FAILED(2005, "Could not execute call to RTD"),
    KEYVAULT_ENCRYPTION_FAILED(2006, "Keyvault encryption failed"),
    KEYVAULT_DECRYPTION_FAILED(2007, "Keyvault decryption failed"),
    INCONSISTENT_MESSAGE(2008, "Inconsistent message");


    @Getter
    private final Integer statusCode;

    @Getter
    private final String message;

}
