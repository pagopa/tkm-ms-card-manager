package it.gov.pagopa.tkm.ms.cardmanager.model.entity;

import lombok.*;

import javax.persistence.*;
import java.time.*;

@Entity
@Table(name = "CITIZEN_CARD")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TkmCitizenCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CITIZEN_ID", nullable = false)
    private TkmCitizen citizen;

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
