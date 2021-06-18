package it.gov.pagopa.tkm.ms.cardmanager.model.entity;

import lombok.*;
import lombok.Builder;

import javax.persistence.*;

@Entity
@Table(name = "CARD_TOKEN")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"id","card"})
public class TkmCardToken {

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
