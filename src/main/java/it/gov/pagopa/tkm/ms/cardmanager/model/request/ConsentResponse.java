package it.gov.pagopa.tkm.ms.cardmanager.model.request;

import lombok.*;
import lombok.Builder;

import javax.validation.constraints.*;
import java.util.*;
import java.util.stream.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsentResponse {

    @NotNull
    private ConsentEntityEnum consent;

    @NotBlank
    private String taxCode;

    @Builder.Default
    private Set<CardServiceConsent> details = new HashSet<>();

    public List<String> retrieveHpans() {
        return details.stream().map(CardServiceConsent::getHpan).collect(Collectors.toList());
    }

    public boolean cardHasConsent(String hpan) {
        switch(consent) {
            case Allow: return true;
            case Partial: return details.stream().filter(d -> hpan.equals(d.getHpan())).anyMatch(CardServiceConsent::hasConsent);
            case Deny:
            default:
                return false;
        }
    }

}
