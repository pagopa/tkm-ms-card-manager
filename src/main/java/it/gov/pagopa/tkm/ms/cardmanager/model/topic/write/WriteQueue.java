package it.gov.pagopa.tkm.ms.cardmanager.model.topic.write;

import com.fasterxml.jackson.annotation.*;
import it.gov.pagopa.tkm.constant.*;
import lombok.*;

import java.time.*;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WriteQueue {

    private String taxCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss:SSSS", timezone = TkmDatetimeConstant.DATE_TIME_TIMEZONE)
    private Instant timestamp;

    private Set<WriteQueueCard> cards = new HashSet<>();

}

