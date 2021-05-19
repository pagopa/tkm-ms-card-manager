package it.gov.pagopa.tkm.ms.cardmanager.controller.impl;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import org.bouncycastle.openpgp.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

//TODO: REMOVE (TEST CONTROLLER)
@RestController
@RequestMapping("/kafka")
public final class KafkaController {

    @Autowired
    private ProducerService producerService;

    @Autowired
    private ObjectMapper mapper;

    @PostMapping
    public void sendMessageToKafkaTopic() throws PGPException, JsonProcessingException {
        ReadQueue readQueue = new ReadQueue(
                "AAABBBCCCDDD1234",
                "1234567890123456789",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu",
                CircuitEnum.AMEX,
                Collections.singletonList(new Token("vvvvvvvvvvvvvvvv", "hhhhhhhhhhhhh"))
        );
        producerService.sendMessage(mapper.writeValueAsString(readQueue));
    }

}
