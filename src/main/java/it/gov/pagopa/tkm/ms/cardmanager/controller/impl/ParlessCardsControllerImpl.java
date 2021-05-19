package it.gov.pagopa.tkm.ms.cardmanager.controller.impl;

import it.gov.pagopa.tkm.ms.cardmanager.controller.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiEndpoints.BASE_PATH_PARLESS_CARDS;

@RestController
@RequestMapping(BASE_PATH_PARLESS_CARDS)
public class ParlessCardsControllerImpl implements ParlessCardsController {

    @Autowired
    private ParlessCardsServiceImpl parlessCardsService;

    @Override
    public List<ParlessCardResponse> getParlessCards(Integer maxRecords) {
        return parlessCardsService.getParlessCards(maxRecords);
    }

}
