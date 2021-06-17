package it.gov.pagopa.tkm.ms.cardmanager.model.topic.write;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.gov.pagopa.tkm.constant.TkmDatetimeConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WriteQueue {

    private String taxCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TkmDatetimeConstant.DATE_TIME_PATTERN, timezone = TkmDatetimeConstant.DATE_TIME_TIMEZONE)
    private Instant timestamp;

    private Set<WriteQueueCard> cards = new HashSet<>();

}

