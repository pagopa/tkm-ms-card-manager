package it.gov.pagopa.tkm.ms.cardmanager.model.entity;

import it.gov.pagopa.tkm.ms.cardmanager.constant.CircuitEnum;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "CARD")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(exclude = "tokens")
@ToString(exclude = "tokens")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TkmCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    @Column(name = "TAX_CODE", nullable = false, length = 16)
    private String taxCode;

    @Column(name = "PAN", nullable = false, length = 32)
    private String pan;

    @Column(name = "HPAN", nullable = false, length = 64)
    private String hpan;

    @Column(name = "PAR", nullable = false, length = 32)
    private String par;

    @Enumerated(EnumType.STRING)
    @Column(name = "CIRCUIT", nullable = false, length = 32)
    private CircuitEnum circuit;

    @Column(name = "LAST_READ_DATE")
    private Instant lastReadDate;

    @Column(name = "LAST_UPDATE_DATE")
    private Instant lastUpdateDate;

    @Column(name = "DELETED")
    private boolean deleted;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "card", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Where(clause = "deleted = false")
    private Set<TkmCardToken> tokens = new HashSet<>();

}
