package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.WriteQueue;
import it.gov.pagopa.tkm.ms.cardmanager.service.ProducerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Log4j2
public class ProducerServiceImpl implements ProducerService {

    @Autowired
    @Qualifier("dltWriteProducer")
    private KafkaTemplate<String, String> dltWriteProducer;

    @Autowired
    private ObjectMapper mapper;

    @Value("${spring.kafka.topics.write-queue.name}")
    private String writeQueueTopic;

    @PostConstruct
    public void init() {
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
    }

    public void sendMessage(WriteQueue writeQueue) throws JsonProcessingException {
        String message = mapper.writeValueAsString(writeQueue);
        log.info("Writing card to queue");
        dltWriteProducer.send(writeQueueTopic, message);
    }

}
