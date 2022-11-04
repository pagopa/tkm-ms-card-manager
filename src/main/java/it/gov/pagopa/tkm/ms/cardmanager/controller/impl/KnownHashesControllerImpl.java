package it.gov.pagopa.tkm.ms.cardmanager.controller.impl;

import it.gov.pagopa.tkm.ms.cardmanager.controller.KnownHashesController;
import it.gov.pagopa.tkm.ms.cardmanager.controller.QueryServiceMultiThreading;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardSubSet;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardTokenSubSet;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.KnownHashesResponse;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import it.gov.pagopa.tkm.ms.cardmanager.util.NumberRange;
import it.gov.pagopa.tkm.ms.cardmanager.util.NumberUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@RestController
@Log4j2
public class KnownHashesControllerImpl implements KnownHashesController {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardTokenRepository cardTokenRepository;

    @Autowired
    private QueryServiceMultiThreading queryServiceMultiThreading;

    @Value("${thread.known-hashes}")
    private int threadKnownHashes;

    @Override
    @Transactional(readOnly = true)
    public KnownHashesResponse getKnownHashes(Long maxRecords, Long hpanOffset, Long htokenOffset) throws ExecutionException, InterruptedException {
        log.info("Retrieving a maximum of " + maxRecords + " hashes with hpan offset " + hpanOffset + " and htoken offset " + htokenOffset);
        KnownHashesResponse response = new KnownHashesResponse();
        TkmCard firstCard = cardRepository.findTopByOrderByIdAsc();
        if (firstCard != null && firstCard.getId() > hpanOffset + maxRecords) {
            log.info("First card has id " + firstCard.getId() + ", returning empty hpan set");
            response.setHpans(new HashSet<>());
            response.setNextHpanOffset(firstCard.getId() - 1);
        } else {
            List<TkmCardSubSet> cards = getTkmCardSubSets(hpanOffset, maxRecords);
            log.info("Found " + CollectionUtils.size(cards) + " hpans");
            response.setHpans(cards.stream().map(TkmCardSubSet::getHpan).collect(Collectors.toSet()));
            response.setNextHpanOffset(cards.stream().mapToLong(TkmCardSubSet::getId).max().orElse(hpanOffset));
        }
        long diff = maxRecords - response.getHpans().size();
        if (diff > 0) {
            TkmCardToken firstToken = cardTokenRepository.findTopByOrderByIdAsc();
            if (firstToken != null && firstToken.getId() > htokenOffset + diff) {
                log.info("First token has id " + firstToken.getId() + ", returning empty htoken set");
                response.setHtokens(new HashSet<>());
                response.setNextHtokenOffset(firstToken.getId() - 1);
            } else {
                List<TkmCardTokenSubSet> tkmCardTokenSubSetFull = getTkmCardTokenSubSets(htokenOffset, diff);
                log.info("Found " + CollectionUtils.size(tkmCardTokenSubSetFull) + " tokens");
                response.setHtokens(tkmCardTokenSubSetFull.stream().map(TkmCardTokenSubSet::getHtoken).collect(Collectors.toSet()));
                response.setNextHtokenOffset(tkmCardTokenSubSetFull.stream().mapToLong(TkmCardTokenSubSet::getId).max().orElse(htokenOffset));
            }
        }
        return response;
    }

    @NotNull
    private List<TkmCardTokenSubSet> getTkmCardTokenSubSets(Long htokenOffset, long diff) throws InterruptedException, ExecutionException {
        List<TkmCardTokenSubSet> tkmCardTokenSubSetFull = new ArrayList<>();
        long max = htokenOffset + diff;
        List<NumberRange> numberRanges = NumberUtils.splitNumberToRange(htokenOffset, max, threadKnownHashes);
        List<Future<List<TkmCardTokenSubSet>>> futureList = new ArrayList<>();
        for (NumberRange numberRange : numberRanges) {
            futureList.add(queryServiceMultiThreading.getTkmCardTokenSubSetAsync(numberRange.getMinIncluded(), numberRange.getMaxExcluded()));
        }
        for (Future<List<TkmCardTokenSubSet>> listFuture : futureList) {
            List<TkmCardTokenSubSet> collection = listFuture.get();
            tkmCardTokenSubSetFull.addAll(collection);
        }
        return tkmCardTokenSubSetFull;
    }

    @NotNull
    private List<TkmCardSubSet> getTkmCardSubSets(Long hpanOffset, long maxRecord) throws InterruptedException, ExecutionException {
        List<TkmCardSubSet> tkmCardSubSetFull = new ArrayList<>();
        long max = hpanOffset + maxRecord;
        List<NumberRange> numberRanges = NumberUtils.splitNumberToRange(hpanOffset, max, threadKnownHashes);
        List<Future<List<TkmCardSubSet>>> futureList = new ArrayList<>();
        for (NumberRange numberRange : numberRanges) {
            futureList.add(queryServiceMultiThreading.getTkmCardSubSetAsync(numberRange.getMinIncluded(), numberRange.getMaxExcluded()));
        }
        for (Future<List<TkmCardSubSet>> listFuture : futureList) {
            tkmCardSubSetFull.addAll(listFuture.get());
        }
        return tkmCardSubSetFull;
    }
}
