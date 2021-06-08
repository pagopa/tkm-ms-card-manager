package it.gov.pagopa.tkm.ms.cardmanager.model.request;

import lombok.*;
import lombok.experimental.*;

import javax.validation.constraints.*;
import java.util.*;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Consent {

    @NotNull
    private ConsentRequestEnum consent;

    @Size(min = 64, max = 64)
    private String hpan;

    private Set<ServiceEnum> services;

    public boolean isPartial() {
        return hpan != null;
    }

}
