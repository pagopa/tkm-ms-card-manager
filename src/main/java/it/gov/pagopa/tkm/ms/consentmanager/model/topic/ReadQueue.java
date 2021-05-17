package it.gov.pagopa.tkm.ms.consentmanager.model.topic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadQueue {

    private String taxCode;
    private String pan;
    private String hpan;
    private String par;
    private CircuitEnum circuit;
    private List<Token> tokens;

}

