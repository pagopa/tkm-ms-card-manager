package it.gov.pagopa.tkm.ms.cardmanager.client.consent.model.response;

import lombok.*;
import lombok.experimental.*;

import java.time.*;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ConsentResponse {

    private ConsentEntityEnum consent;

    private Instant lastUpdateDate;

    private Set<CardServiceConsent> details;

}
