package it.gov.pagopa.tkm.ms.cardmanager.constant;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;
import java.util.List;

public class CardRepositoryMock {

    public static Page<TkmCard> getOnePageTkmCard() {
        TkmCard tkmCard = TkmCard.builder().hpan(Constant.HASH_1).build();
        TkmCard tkmCard2 = TkmCard.builder().hpan(Constant.HASH_2).build();
        List<TkmCard> tkmCards = Arrays.asList(tkmCard, tkmCard2);
        return new PageImpl<>(tkmCards);
    }

}
