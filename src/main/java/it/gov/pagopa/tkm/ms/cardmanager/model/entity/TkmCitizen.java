package it.gov.pagopa.tkm.ms.cardmanager.model.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "CITIZEN")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TkmCitizen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    @Column(name = "TAX_CODE", unique = true, nullable = false)
    private String taxCode;

}
