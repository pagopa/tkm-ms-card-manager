package it.gov.pagopa.tkm.ms.cardmanager.model.entity;

import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "CARD")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"tokens", "pan", "citizenCards"})
@ToString(exclude = {"tokens", "citizenCards"})
public class TkmCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    @Column(name = "PAN", length = 500)
    private String pan;

    @Column(name = "HPAN", length = 64)
    private String hpan;

    @Column(name = "PAR", length = 32)
    private String par;

    @Enumerated(EnumType.STRING)
    @Column(name = "CIRCUIT", length = 32)
    private CircuitEnum circuit;

    @Column(name = "LAST_READ_DATE")
    private Instant lastReadDate;

    @Column(name = "CREATION_DATE")
    private Instant creationDate;

    @Column(name = "LAST_UPDATE_DATE")
    private Instant lastUpdateDate;

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "card")
    private List<TkmCitizenCard> citizenCards = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "card", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Where(clause = "deleted = false")
    private Set<TkmCardToken> tokens = new HashSet<>();

}
