package it.gov.pagopa.tkm.ms.cardmanager.constant;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCitizen;

import java.time.Instant;

public class CitizenRepositoryMock {
    public static TkmCitizen getCitizenFull(Instant instant) {

        return TkmCitizen.builder().creationDate(instant)
                .taxCode(Constant.TAX_CODE_1)
                .deleted(false).build();
    }
}
