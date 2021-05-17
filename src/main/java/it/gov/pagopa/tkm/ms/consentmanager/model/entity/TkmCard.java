package it.gov.pagopa.tkm.ms.consentmanager.model.entity;

import lombok.*;
import lombok.experimental.*;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "CARD")
@Data
@Accessors(chain = true)
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

    @Column(name = "DELETED")
    private boolean deleted;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "token")
    private Set<TkmToken> tokens = new HashSet<>();

}
