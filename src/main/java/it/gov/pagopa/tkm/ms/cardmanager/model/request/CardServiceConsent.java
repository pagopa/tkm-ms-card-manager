package it.gov.pagopa.tkm.ms.cardmanager.model.request;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardServiceConsent {

    private String hpan;

    private Set<ServiceConsent> serviceConsents;

    public boolean hasConsent() {
        return serviceConsents.stream().anyMatch(s -> ConsentRequestEnum.Allow.equals(s.getConsent()));
    }

}
