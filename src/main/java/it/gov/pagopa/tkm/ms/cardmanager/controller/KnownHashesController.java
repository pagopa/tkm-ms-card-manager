package it.gov.pagopa.tkm.ms.cardmanager.controller;

import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiEndpoints.BASE_PATH_KNOWN_HASHES;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams.*;

@Validated
@RequestMapping(BASE_PATH_KNOWN_HASHES)
public interface KnownHashesController {

    String MAX_NUMBER_OF_RECORDS_DEFAULT = "100000";
    String OFFSET_DEFAULT = "0";
    long MIN_VALUE = 10L;
    long MAX_VALUE = 1000000L;

    @GetMapping
    KnownHashesResponse getKnownHashes(
            @Valid
            @RequestParam(value = MAX_NUMBER_OF_RECORDS_PARAM, defaultValue = MAX_NUMBER_OF_RECORDS_DEFAULT)
            @Range(min = MIN_VALUE, max = MAX_VALUE)
            Long maxRecords,
            @RequestParam(value = HPAN_OFFSET_PARAM, defaultValue = OFFSET_DEFAULT)
            Long hpanOffset,
            @RequestParam(value = HTOKEN_OFFSET_PARAM, defaultValue = OFFSET_DEFAULT)
            Long htokenOffset
    );

}
