package it.gov.pagopa.tkm.ms.consentmanager.service.impl;

import it.gov.pagopa.tkm.ms.consentmanager.crypto.*;
import lombok.extern.log4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.tkm.ms.consentmanager.constant.Constants.*;

@Service
@Log4j2
public final class ConsumerService {

    @Autowired
    private PgpUtils pgpUtils;

    @KafkaListener(topics = TKM_READ_TOKEN_PAR_PAN_TOPIC)
    public void consume(String message) throws Exception {
        String decryptedMessage = pgpUtils.decrypt(message);
        log.info("Consumed message: " + decryptedMessage);
    }

}
