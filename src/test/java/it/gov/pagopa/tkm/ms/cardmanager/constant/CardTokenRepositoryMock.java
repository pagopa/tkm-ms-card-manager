package it.gov.pagopa.tkm.ms.cardmanager.constant;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;
import java.util.List;

public class CardTokenRepositoryMock {

    public static Page<TkmCardToken> getOnePageTkmCardToken() {
        TkmCardToken tkmCardToken = new TkmCardToken().setHtoken(Constant.HASH_1);
        TkmCardToken tkmCardToken2 = new TkmCardToken().setHtoken(Constant.HASH_2);
        List<TkmCardToken> tkmCards = Arrays.asList(tkmCardToken, tkmCardToken2);
        return new PageImpl<>(tkmCards);
    }
}
