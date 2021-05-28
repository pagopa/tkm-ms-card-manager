package it.gov.pagopa.tkm.ms.cardmanager.model.topic;

import com.fasterxml.jackson.databind.annotation.*;
import it.gov.pagopa.tkm.annotation.*;
import it.gov.pagopa.tkm.jsondeserializer.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.*;

import javax.validation.*;
import javax.validation.constraints.*;
import java.util.*;

import static it.gov.pagopa.tkm.constant.Constants.FISCAL_CODE_REGEX;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@CheckAtLeastOneNotEmpty(fieldNames = {"pan", "par"})
public class ReadQueue {

    @NotEmpty
    @Pattern(regexp = FISCAL_CODE_REGEX)
    @JsonDeserialize(using = ToUpperCaseDeserializer.class)
    private String taxCode;

    @Size(min = 12, max = 20)
    private String pan;

    @Size(min = 64, max = 64)
    @JsonDeserialize(using = ToLowerCaseDeserializer.class)
    private String hpan;

    @JsonDeserialize(using = ToLowerCaseDeserializer.class)
    private String par;

    @NotNull
    private CircuitEnum circuit;

    private List<@Valid Token> tokens = new ArrayList<>();

}

