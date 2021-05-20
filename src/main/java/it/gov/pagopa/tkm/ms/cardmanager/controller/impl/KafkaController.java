package it.gov.pagopa.tkm.ms.cardmanager.controller.impl;

import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

//TODO: REMOVE (TEST CONTROLLER)
@RestController
@RequestMapping("/kafka")
public final class KafkaController {

    @Autowired
    private ProducerServiceImpl producerService;

    @PostMapping
    public void sendMessageToKafkaTopic(@RequestBody String readQueue) {
        producerService.sendMessage(readQueue);
    }

}
