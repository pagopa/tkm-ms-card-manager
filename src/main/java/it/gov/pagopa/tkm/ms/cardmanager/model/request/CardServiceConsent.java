package it.gov.pagopa.tkm.ms.cardmanager.model.request;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import javax.validation.*;
import javax.validation.constraints.*;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardServiceConsent {

    @NotBlank
    private String hpan;

    @NotEmpty
    private Set<@Valid ServiceConsent> serviceConsents;

    public boolean hasConsent() {
        return serviceConsents.stream().anyMatch(s -> ConsentRequestEnum.Allow.equals(s.getConsent()));
    }

}
