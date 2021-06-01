package it.gov.pagopa.tkm.ms.cardmanager.model.topic.read;

import com.fasterxml.jackson.databind.annotation.*;
import it.gov.pagopa.tkm.jsondeserializer.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadQueueToken {

    @NotEmpty
    private String token;

    @JsonDeserialize(using = ToLowerCaseDeserializer.class)
    @Size(min = 64, max = 64)
    private String hToken;

}