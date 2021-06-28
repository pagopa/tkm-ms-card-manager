package it.gov.pagopa.tkm.ms.cardmanager.model.entity;

import lombok.*;

import java.io.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CitizenCardId implements Serializable {

    private Long citizen;

    private Long card;

}
