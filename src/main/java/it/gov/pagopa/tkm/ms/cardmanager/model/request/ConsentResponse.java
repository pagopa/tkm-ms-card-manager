package it.gov.pagopa.tkm.ms.cardmanager.model.request;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import lombok.experimental.*;

import javax.validation.constraints.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ConsentResponse {

    private ConsentEntityEnum consent;

    @NotEmpty
    private String taxCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Europe/Rome")
    private Instant lastUpdateDate;

    private Set<CardServiceConsent> details;

    public List<String> getHpans() {
        return details.stream().map(CardServiceConsent::getHpan).collect(Collectors.toList());
    }

    public boolean cardHasConsent(String hpan) {
        return details.stream().filter(d -> hpan.equals(d.getHpan())).anyMatch(CardServiceConsent::hasConsent);
    }

}
