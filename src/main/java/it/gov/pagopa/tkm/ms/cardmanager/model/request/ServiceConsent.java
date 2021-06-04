package it.gov.pagopa.tkm.ms.cardmanager.model.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceConsent {

    private ConsentRequestEnum consent;

    private ServiceEnum service;

}
