package it.gov.pagopa.tkm.ms.cardmanager.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NumberRange {
    private long minIncluded;
    private long maxExcluded;
}
