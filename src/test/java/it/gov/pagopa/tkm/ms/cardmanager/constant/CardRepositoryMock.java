package it.gov.pagopa.tkm.ms.cardmanager.constant;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class CardRepositoryMock {

    public static List<TkmCard> getTkmCardsList() {
        TkmCard tkmCard = TkmCard.builder().id(1L).hpan(Constant.HASH_1).build();
        TkmCard tkmCard2 = TkmCard.builder().id(2L).hpan(Constant.HASH_2).build();
        return Arrays.asList(tkmCard, tkmCard2);
    }

    public static TkmCard getTkmCardFull() {
        return TkmCard.builder()
                .taxCode(Constant.TAX_CODE_1)
                .hpan(Constant.HASH_1)
                .par("par")
                .circuit(CircuitEnum.VISA)
                .tokens(new HashSet<>(Collections.singletonList(CardTokenRepositoryMock.getCardtoken())))
                .build();
    }
}
