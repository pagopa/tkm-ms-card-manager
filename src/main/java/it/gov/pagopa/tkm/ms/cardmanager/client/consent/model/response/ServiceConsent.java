package it.gov.pagopa.tkm.ms.cardmanager.client.consent.model.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceConsent {

    private ConsentRequestEnum consent;

    private ServiceEnum service;

}
