package it.gov.pagopa.tkm.ms.cardmanager.model.topic.write;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import lombok.*;

import static it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.TokenActionEnum.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WriteQueueToken {

    public WriteQueueToken(TkmCardToken tkmCardToken) {
        htoken = tkmCardToken.getHtoken();
        haction = tkmCardToken.isDeleted() ? DELETE : INSERT_UPDATE;
    }

    private String htoken;

    private TokenActionEnum haction;

}
