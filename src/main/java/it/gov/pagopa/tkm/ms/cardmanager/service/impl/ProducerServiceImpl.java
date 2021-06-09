package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.*;
import lombok.extern.log4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ProducerServiceImpl implements ProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Value("${spring.kafka.topics.write-queue}")
    private String writeQueueTopic;

    public void sendMessage(WriteQueue writeQueue) throws JsonProcessingException {
        String message = mapper.writeValueAsString(writeQueue);
        log.info("Writing card to queue: " + message);
        kafkaTemplate.send(writeQueueTopic, message);
    }

}
