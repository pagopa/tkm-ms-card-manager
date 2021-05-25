package it.gov.pagopa.tkm.ms.cardmanager.model.topic;

import com.fasterxml.jackson.databind.annotation.*;
import it.gov.pagopa.tkm.jsondeserializer.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    private String token;

    @JsonDeserialize(using = ToLowerCaseDeserializer.class)
    private String hToken;

}