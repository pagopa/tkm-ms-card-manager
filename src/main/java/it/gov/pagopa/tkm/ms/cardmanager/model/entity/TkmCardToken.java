package it.gov.pagopa.tkm.ms.cardmanager.model.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "CARD_TOKEN")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TkmCardToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID", nullable = false)
    private TkmCard card;

    @Column(name = "TOKEN", nullable = false, length = 500)
    private String token;

    @EqualsAndHashCode.Include
    @Column(name = "HTOKEN", unique = true, nullable = false, length = 64)
    private String htoken;

    @Column(name = "LAST_READ_DATE")
    private Instant lastReadDate;

    @EqualsAndHashCode.Include
    @Builder.Default
    @Column(name = "DELETED")
    private boolean deleted = false;

    @Column(name = "CREATION_DATE")
    private Instant creationDate;

    @Column(name = "LAST_UPDATE_DATE")
    private Instant lastUpdateDate;

}
