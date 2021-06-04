package it.gov.pagopa.tkm.ms.cardmanager.model.response;

import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import lombok.*;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParlessCardResponse {

    private String taxCode;

    private String pan;

    private Set<String> tokens;

    private CircuitEnum circuit;

}
