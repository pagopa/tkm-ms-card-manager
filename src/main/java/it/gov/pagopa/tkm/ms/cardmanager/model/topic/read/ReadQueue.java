package it.gov.pagopa.tkm.ms.cardmanager.model.topic.read;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import it.gov.pagopa.tkm.jsondeserializer.ToLowerCaseDeserializer;
import it.gov.pagopa.tkm.jsondeserializer.ToUpperCaseDeserializer;
import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.tkm.constant.Constants.FISCAL_CODE_REGEX;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadQueue {

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

    private List<@Valid ReadQueueToken> tokens = new ArrayList<>();

}

