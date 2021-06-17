package it.gov.pagopa.tkm.ms.cardmanager.constant;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class CardRepositoryMock {

    public static Page<TkmCard> getOnePageTkmCard() {
        TkmCard tkmCard = new TkmCard().setHpan(Constant.HASH_1);
        TkmCard tkmCard2 = new TkmCard().setHpan(Constant.HASH_2);
        List<TkmCard> tkmCards = Arrays.asList(tkmCard, tkmCard2);
        return new PageImpl<>(tkmCards);
    }

    public static TkmCard getTkmCardFull() {
        return new TkmCard()
                .setTaxCode(Constant.TAX_CODE_1)
                .setHpan(Constant.HASH_1)
                .setPar("par")
                .setCircuit(CircuitEnum.VISA)
                .setTokens(new HashSet<>(Collections.singletonList(CardTokenRepositoryMock.getCardtoken())));
    }
}
