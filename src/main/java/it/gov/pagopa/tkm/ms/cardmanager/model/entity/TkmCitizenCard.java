package it.gov.pagopa.tkm.ms.cardmanager.model.entity;

import lombok.*;

import javax.persistence.*;
import java.time.*;

@Entity
@Table(name = "CITIZEN_CARD")
@IdClass(CitizenCardId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TkmCitizenCard {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CITIZEN_ID", nullable = false)
    private TkmCitizen citizen;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID", nullable = false)
    private TkmCard card;

    @Column(name = "CREATION_DATE")
    private Instant creationDate;

    @Column(name = "LAST_UPDATE_DATE")
    private Instant lastUpdateDate;

    @Column(name = "DELETED")
    private boolean deleted;

}
