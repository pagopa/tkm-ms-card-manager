package it.gov.pagopa.tkm.ms.cardmanager.constant;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardSubSet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class CardRepositoryMock {

    public static List<TkmCardSubSet> getTkmCardsSubSetList() {
        return Arrays.asList(getTkmCardSubSet(1, Constant.HASH_1), getTkmCardSubSet(2, Constant.HASH_2));
    }

    @NotNull
    private static TkmCardSubSet getTkmCardSubSet(long id, String hpan) {
        return new TkmCardSubSet() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public String getHpan() {
                return hpan;
            }
        };
    }

    public static TkmCard getTkmCardFull() {
        return TkmCard.builder()
                .hpan(Constant.HASH_1)
                .pan(Constant.PAN)
                .par("par")
                .circuit(CircuitEnum.VISA)
                .tokens(new HashSet<>(Collections.singletonList(CardTokenRepositoryMock.getCardtoken())))
                .build();
    }
}
