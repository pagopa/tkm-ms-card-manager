package it.gov.pagopa.tkm.ms.consentmanager.service.impl;

import it.gov.pagopa.tkm.ms.consentmanager.crypto.*;
import lombok.extern.log4j.*;
import org.bouncycastle.openpgp.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.tkm.ms.consentmanager.constant.Constants.TKM_READ_TOKEN_PAR_PAN_TOPIC;

@Service
@Log4j2
public final class ProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private PgpUtils pgpUtils;

    public void sendMessage(String message) throws PGPException {
        String encryptedMessage = new String(pgpUtils.encrypt(message.getBytes(), true));
        kafkaTemplate.send(TKM_READ_TOKEN_PAR_PAN_TOPIC, encryptedMessage);
        log.info("Sent message: " + encryptedMessage);
    }

}
