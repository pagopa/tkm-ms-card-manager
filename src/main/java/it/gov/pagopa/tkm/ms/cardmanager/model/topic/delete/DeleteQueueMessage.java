package it.gov.pagopa.tkm.ms.cardmanager.model.topic.delete;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import it.gov.pagopa.tkm.constant.*;
import it.gov.pagopa.tkm.jsondeserializer.ToUpperCaseDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.Instant;

import static it.gov.pagopa.tkm.constant.Constants.FISCAL_CODE_REGEX;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteQueueMessage {
    @NotBlank
    @Length(min = 16, max = 16)
    @Pattern(regexp = FISCAL_CODE_REGEX)
    @JsonDeserialize(using = ToUpperCaseDeserializer.class)
    private String taxCode;

    @NotBlank
    @Length(min = 64, max = 64)
    private String hpan;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss:SSSS", timezone = TkmDatetimeConstant.DATE_TIME_TIMEZONE)
    private Instant timestamp;
}
