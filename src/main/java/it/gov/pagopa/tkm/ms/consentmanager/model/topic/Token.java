package it.gov.pagopa.tkm.ms.consentmanager.model.topic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    private String token;
    private String hToken;

}