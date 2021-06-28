package it.gov.pagopa.tkm.ms.cardmanager.model.entity;

import lombok.*;

import javax.persistence.*;
import java.time.*;

@Entity
@Table(name = "CARD_TOKEN")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"id", "card", "token"})
public class TkmCardToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID", nullable = false)
    private TkmCard card;

    @Column(name = "TOKEN", nullable = false, length = 500)
    private String token;

    @Column(name = "HTOKEN", nullable = false, length = 64)
    private String htoken;

    @Column(name = "LAST_READ_DATE")
    private Instant lastReadDate;

    @Column(name = "DELETED")
    private boolean deleted = false;

}
