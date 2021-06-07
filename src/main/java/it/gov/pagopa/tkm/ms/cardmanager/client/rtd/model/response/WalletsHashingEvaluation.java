package it.gov.pagopa.tkm.ms.cardmanager.client.rtd.model.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletsHashingEvaluation {

    private String hashPan;

    private String salt;

}
