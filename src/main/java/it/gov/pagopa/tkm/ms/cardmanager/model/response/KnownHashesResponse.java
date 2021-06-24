package it.gov.pagopa.tkm.ms.cardmanager.model.response;

import lombok.*;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnownHashesResponse {

    private Set<String> hpans = new HashSet<>();

    private Set<String> htokens = new HashSet<>();

    private Long nextHpanOffset;

    private Long nextHtokenOffset;

}
