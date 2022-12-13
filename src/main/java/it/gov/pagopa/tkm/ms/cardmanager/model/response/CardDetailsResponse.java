package it.gov.pagopa.tkm.ms.cardmanager.model.response;

import lombok.*;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardDetailsResponse {

    private String hpan;

    private String par;

    private Set<String> htokens;

}