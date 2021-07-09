package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.exception.KafkaProcessMessageException;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.service.MessageValidatorService;
import it.gov.pagopa.tkm.ms.cardmanager.service.ReaderQueueService;
import it.gov.pagopa.tkm.service.PgpStaticUtils;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.openpgp.PGPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Future;

@Service
@Log4j2
public class ReaderQueueServiceImpl implements ReaderQueueService {
    @Value("${keyvault.tkmReadTokenParPanPvtPgpKey}")
    private String tkmReadTokenParPanPvtPgpKey;

    @Value("${keyvault.tkmReadTokenParPanPvtPgpKeyPassphrase}")
    private String tkmReadTokenParPanPvtPgpKeyPassphrase;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CardServiceImpl cardService;

    @Autowired
    private MessageValidatorService validatorService;

    @Async
    @Transactional
    public Future<Void> workOnMessage(String message){
        log.debug("Reading message from queue: " + message);
        String decryptedMessage;
        try {
            decryptedMessage = PgpStaticUtils.decrypt(message, tkmReadTokenParPanPvtPgpKey, tkmReadTokenParPanPvtPgpKeyPassphrase);
            log.trace("Decrypted message from queue: " + decryptedMessage);
            ReadQueue readQueue = mapper.readValue(decryptedMessage, ReadQueue.class);
            validatorService.validateMessage(readQueue);
            cardService.updateOrCreateCard(readQueue);
        } catch (KafkaProcessMessageException | PGPException | CardException | JsonProcessingException e) {
            log.error(e);
        }
        return null;
    }
}
