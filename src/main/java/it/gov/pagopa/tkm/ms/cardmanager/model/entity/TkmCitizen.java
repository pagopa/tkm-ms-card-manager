package it.gov.pagopa.tkm.ms.cardmanager.model.entity;

import lombok.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CITIZEN")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "citizenCards")
@ToString(exclude = "citizenCards")
public class TkmCitizen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    @Column(name = "TAX_CODE", unique = true, nullable = false)
    private String taxCode;

    @Column(name = "CREATION_DATE", nullable = false)
    private Instant creationDate;

    @Builder.Default
    @Column(name = "DELETED")
    private boolean deleted = false;

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "citizen")
    @Where(clause = "deleted = false")
    private List<TkmCitizenCard> citizenCards = new ArrayList<>();

}
