package it.gov.pagopa.tkm.ms.cardmanager.model.topic;

import com.fasterxml.jackson.databind.annotation.*;
import it.gov.pagopa.tkm.jsondeserializer.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadQueue {

    @JsonDeserialize(using = ToUpperCaseDeserializer.class)
    private String taxCode;

    private String pan;

    @JsonDeserialize(using = ToLowerCaseDeserializer.class)
    private String hpan;

    @JsonDeserialize(using = ToLowerCaseDeserializer.class)
    private String par;

    private CircuitEnum circuit;

    private List<Token> tokens;

}

