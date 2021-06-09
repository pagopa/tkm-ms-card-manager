package it.gov.pagopa.tkm.ms.cardmanager.model.request;

import lombok.*;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceConsent {

    @NotNull
    private ConsentRequestEnum consent;

    @NotNull
    private ServiceEnum service;

}
