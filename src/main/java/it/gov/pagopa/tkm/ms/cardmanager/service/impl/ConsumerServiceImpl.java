package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.consentmanager.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.rtd.*;
import it.gov.pagopa.tkm.ms.cardmanager.client.rtd.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.exception.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.*;
import it.gov.pagopa.tkm.service.*;
import lombok.extern.log4j.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.*;

import javax.validation.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.*;
import static it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.CardActionEnum.*;

@Service
@Log4j2
public class ConsumerServiceImpl implements ConsumerService {

    @Autowired
    private PgpUtils pgpUtils;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private RtdHashingClient rtdHashingClient;

    @Autowired
    private Validator validator;

    @Autowired
    private ConsentClient consentClient;

    @Autowired
    private ProducerServiceImpl producerService;

    @Value("${keyvault.apimSubscriptionTkmRtd}")
    private String apimRtdSubscriptionKey;

    @Override
    @KafkaListener(topics = "#{'${spring.kafka.topics.read-queue}'}")
    public void consume(String message) throws JsonProcessingException {
        log.debug("Reading message from queue: " + message);
        String decryptedMessage;
        try {
            decryptedMessage = pgpUtils.decrypt(message);
        } catch (Exception e) {
            throw new CardException(MESSAGE_DECRYPTION_FAILED);
        }
        log.trace("Decrypted message from queue: " + decryptedMessage);
        ReadQueue readQueue = mapper.readValue(decryptedMessage, ReadQueue.class);
        validateReadQueue(readQueue);
        updateOrCreateCard(readQueue);
    }

    private void validateReadQueue(ReadQueue readQueue) {
        if (!CollectionUtils.isEmpty(validator.validate(readQueue))) {
            throw new CardException(MESSAGE_VALIDATION_FAILED);
        }
    }

    private void updateOrCreateCard(ReadQueue readQueue) {
        String taxCode = readQueue.getTaxCode();
        String par = readQueue.getPar();
        String pan = readQueue.getPan();
        String hpan = (readQueue.getHpan() == null && pan != null) ? callRtdForHash(pan) : readQueue.getHpan();
        TkmCard card = findCard(taxCode, hpan, par);
        Set<TkmCardToken> oldTokens = new HashSet<>();
        boolean merged = false;
        if (card == null) {
            log.info("Card not found on database, creating new one");
            card = createCard(taxCode, pan, hpan, par, readQueue.getCircuit());
        } else {
            log.info("Card found on database, updating");
            oldTokens.addAll(card.getTokens());
            merged = updateCard(card, pan, hpan, par);
        }
        manageTokens(card, readQueue.getTokens());
        log.info("Merged tokens: " + card.getTokens().stream().map(TkmCardToken::getHtoken).collect(Collectors.joining(", ")));
        cardRepository.save(card);
        writeOnQueueIfComplete(card, oldTokens, merged);
    }

    private TkmCard findCard(String taxCode, String hpan, String par) {
        TkmCard card = null;
        if (hpan != null) {
            log.info("Searching card for taxCode " + taxCode + " and hpan " + hpan);
            card = cardRepository.findByTaxCodeAndHpanAndDeletedFalse(taxCode, hpan);
        }
        if (card == null && par != null) {
            log.info("Card not found by hpan, searching by par " + par);
            card = cardRepository.findByTaxCodeAndParAndDeletedFalse(taxCode, par);
        }
        return card;
    }

    private TkmCard createCard(String taxCode, String pan, String hpan, String par, CircuitEnum circuit) {
        return new TkmCard()
                .setTaxCode(taxCode)
                .setCircuit(circuit)
                .setPan(pan)
                .setHpan(hpan)
                .setPar(par);
    }

    private boolean updateCard(TkmCard foundCard, String pan, String hpan, String par) {
        TkmCard preexistingCard = null;
        String taxCode = foundCard.getTaxCode();
        boolean toMerge = false;
        if (par != null && foundCard.getPar() == null) {
            preexistingCard = cardRepository.findByTaxCodeAndParAndDeletedFalse(taxCode, par);
            foundCard.setPar(par);
            toMerge = true;
        } else if (hpan != null && foundCard.getHpan() == null) {
            preexistingCard = cardRepository.findByTaxCodeAndHpanAndDeletedFalse(taxCode, hpan);
            foundCard.setPan(pan).setHpan(hpan);
            toMerge = true;
        }
        if (preexistingCard != null) {
            log.info("Preexisting card found with " + (par != null ? "par " + par : "hpan " + hpan) + ", merging");
            mergeTokens(preexistingCard.getTokens(), foundCard.getTokens());
            cardRepository.delete(preexistingCard);
        }
        return toMerge;
    }

    private void manageTokens(TkmCard card, List<ReadQueueToken> readQueueTokens) {
        Set<TkmCardToken> newTokens = queueTokensToTkmTokens(card, readQueueTokens);
        mergeTokens(card.getTokens(), newTokens);
        card.getTokens().addAll(newTokens);
    }

    private void mergeTokens(Set<TkmCardToken> oldTokens, Set<TkmCardToken> newTokens) {
        oldTokens.stream().filter(t -> !newTokens.contains(t)).forEach(t -> t.setDeleted(true));
    }

    private String callRtdForHash(String toHash) {
        log.trace("Calling RTD for hash of " + toHash);
        return rtdHashingClient.getHash(new WalletsHashingEvaluationInput(toHash), apimRtdSubscriptionKey).getHashPan();
    }

    private Set<TkmCardToken> queueTokensToTkmTokens(TkmCard card, List<ReadQueueToken> readQueueTokens) {
        return readQueueTokens.stream().map(t -> new TkmCardToken()
                        .setCard(card)
                        .setToken(t.getToken())
                        .setHtoken(StringUtils.isNotBlank(t.getHToken()) ? t.getHToken() : callRtdForHash(t.getToken()))
        ).collect(Collectors.toSet());
    }

    private void writeOnQueueIfComplete(TkmCard card, Set<TkmCardToken> oldTokens, boolean merged) {
        if (StringUtils.isAnyBlank(card.getPan(), card.getPar())) {
            log.info("Card missing pan or par, not writing on queue");
            return;
        }
        try {
            boolean cardHasConsent = getConsentForCard(card);
            WriteQueueCard writeQueueCard = new WriteQueueCard(
                    card.getHpan(),
                    cardHasConsent ? INSERT_UPDATE : REVOKE,
                    card.getPar(),
                    cardHasConsent ? getTokensDiff(oldTokens, card.getTokens(), merged) : null
            );
            WriteQueue writeQueue = new WriteQueue(
                card.getTaxCode(),
                Instant.now(),
                Collections.singleton(writeQueueCard)
            );
            producerService.sendMessage(writeQueue);
        } catch (Exception e) {
            throw new CardException(ErrorCodeEnum.MESSAGE_WRITE_FAILED);
        }
    }

    private Set<WriteQueueToken> getTokensDiff(Set<TkmCardToken> oldTokens, Set<TkmCardToken> newTokens, boolean merged) {
        return merged ?
                oldTokens.stream().map(WriteQueueToken::new).collect(Collectors.toSet())
                : newTokens.stream().filter(t -> t.isDeleted() || !oldTokens.contains(t)).map(WriteQueueToken::new).collect(Collectors.toSet());
    }

    private boolean getConsentForCard(TkmCard card) {
        ConsentResponse consentResponse = consentClient.getConsent(card.getTaxCode(), card.getHpan(), null);
        return consentResponse.cardHasConsent(card.getHpan());
    }

}
