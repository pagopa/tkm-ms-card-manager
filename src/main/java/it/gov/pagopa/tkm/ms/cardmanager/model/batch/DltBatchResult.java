package it.gov.pagopa.tkm.ms.cardmanager.model.batch;

import it.gov.pagopa.tkm.model.BaseResultDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class DltBatchResult extends BaseResultDetails {
    private long numRecordProcessed = 0;
    private Map<String, Integer> recordsDetails;
}
