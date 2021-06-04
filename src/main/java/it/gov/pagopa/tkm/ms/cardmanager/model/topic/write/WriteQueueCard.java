package it.gov.pagopa.tkm.ms.cardmanager.model.topic.write;

import lombok.*;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WriteQueueCard {

    private String hpan;

    private CardActionEnum action;

    private String par;

    private Set<WriteQueueToken> htokens;

}
