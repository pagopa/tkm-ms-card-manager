package it.gov.pagopa.tkm.ms.cardmanager.model.request;

import lombok.*;
import lombok.experimental.*;

import javax.validation.constraints.*;
import java.util.*;
import java.util.stream.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ConsentResponse {

    @NotNull
    private ConsentEntityEnum consent;

    @NotBlank
    private String taxCode;

    private Set<CardServiceConsent> details = new HashSet<>();

    public List<String> getHpans() {
        return details.stream().map(CardServiceConsent::getHpan).collect(Collectors.toList());
    }

    public boolean cardHasConsent(String hpan) {
        return details.stream().filter(d -> hpan.equals(d.getHpan())).anyMatch(CardServiceConsent::hasConsent);
    }

}
