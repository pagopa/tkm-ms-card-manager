package it.gov.pagopa.tkm.ms.consentmanager.controller.impl;

import it.gov.pagopa.tkm.ms.consentmanager.model.topic.*;
import it.gov.pagopa.tkm.ms.consentmanager.service.impl.*;
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

    @PostMapping
    public void sendMessageToKafkaTopic() throws PGPException {
        ReadQueue readQueue = new ReadQueue(
                "AAABBBCCCDDD1234",
                "1234567890123456789",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu",
                CircuitEnum.AMEX,
                Collections.singletonList(new Token("vvvvvvvvvvvvvvvv", "hhhhhhhhhhhhh"))
        );
        producerService.sendMessage(readQueue.toString());
    }

}
