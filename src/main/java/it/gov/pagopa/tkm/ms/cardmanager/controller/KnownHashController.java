package it.gov.pagopa.tkm.ms.cardmanager.controller;

import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Set;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiEndpoints.BASE_PATH_KNOWN_HASH;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams.MAX_NUMBER_OF_RECORD;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams.PAGE_NUMBER;

@Validated
@RequestMapping(BASE_PATH_KNOWN_HASH)
public interface KnownHashController {

    String MAX_NUMBER_OF_RECORD_DEFAULT = "100000";
    String PAGE_NUMBER_DEFAULT = "0";
    int MIN_VALUE = 10;
    int MAX_VALUE = 1000000;

    @GetMapping("pan")
    Set<String> getKnownHashpanSet(@Valid @RequestParam(value = MAX_NUMBER_OF_RECORD, defaultValue = MAX_NUMBER_OF_RECORD_DEFAULT) @Range(min = MIN_VALUE, max = MAX_VALUE) Integer maxRecords,
                                   @RequestParam(value = PAGE_NUMBER, defaultValue = PAGE_NUMBER_DEFAULT) Integer pageNumber, HttpServletResponse response);

    @GetMapping("token")
    Set<String> getKnownHashTokenSet(@Valid @RequestParam(value = MAX_NUMBER_OF_RECORD, defaultValue = MAX_NUMBER_OF_RECORD_DEFAULT) @Range(min = MIN_VALUE, max = MAX_VALUE) Integer maxRecords,
                                     @RequestParam(value = PAGE_NUMBER, defaultValue = PAGE_NUMBER_DEFAULT) Integer pageNumber, HttpServletResponse response);

}
