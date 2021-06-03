package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.Constants.TKM_WRITE_TOKEN_PAR_PAN_TOPIC;

@Service
public class ProducerServiceImpl implements ProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper mapper;

    public void sendMessage(WriteQueue writeQueue) throws JsonProcessingException {
        String message = mapper.writeValueAsString(writeQueue);
        kafkaTemplate.send(TKM_WRITE_TOKEN_PAR_PAN_TOPIC, message);
    }

}
