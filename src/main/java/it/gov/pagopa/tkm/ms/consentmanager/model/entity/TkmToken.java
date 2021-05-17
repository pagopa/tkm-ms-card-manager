package it.gov.pagopa.tkm.ms.consentmanager.model.entity;

import lombok.*;
import lombok.experimental.*;

import javax.persistence.*;

@Entity
@Table(name = "TKM_TOKEN")
@Data
@Accessors(chain = true)
public class TkmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID", nullable = false)
    private TkmCard card;

    @Column(name = "TOKEN", nullable = false, length = 32)
    private String token;

    @Column(name = "HTOKEN", nullable = false, length = 64)
    private String htoken;

    @Column(name = "DELETED")
    private boolean deleted;

}
