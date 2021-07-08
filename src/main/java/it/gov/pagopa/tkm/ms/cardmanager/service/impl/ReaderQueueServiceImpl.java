package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.service.MessageValidatorService;
import it.gov.pagopa.tkm.ms.cardmanager.service.ReaderQueueService;
import it.gov.pagopa.tkm.service.PgpStaticUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Future;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.MESSAGE_DECRYPTION_FAILED;

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
    public Future<Void> workOnMessage(String message) throws JsonProcessingException {
        log.debug("Reading message from queue: " + message);
        String decryptedMessage;
        try {
            decryptedMessage = PgpStaticUtils.decrypt(message, tkmReadTokenParPanPvtPgpKey, tkmReadTokenParPanPvtPgpKeyPassphrase);
        } catch (Exception e) {
            log.error(e);
            throw new CardException(MESSAGE_DECRYPTION_FAILED);
        }
        log.trace("Decrypted message from queue: " + decryptedMessage);
        ReadQueue readQueue = mapper.readValue(decryptedMessage, ReadQueue.class);
        validatorService.validateMessage(readQueue);
        cardService.updateOrCreateCard(readQueue);
        return null;
    }
}
