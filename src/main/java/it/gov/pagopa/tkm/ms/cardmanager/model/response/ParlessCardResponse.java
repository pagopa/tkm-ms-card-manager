package it.gov.pagopa.tkm.ms.cardmanager.model.response;

import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import lombok.*;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParlessCardResponse {

    private String pan;

    private String hpan;

    private CircuitEnum circuit;

    private Set<ParlessCardToken> tokens;

}
