package it.gov.pagopa.tkm.ms.cardmanager.constant;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;

import java.util.Arrays;
import java.util.List;

public class CardTokenRepositoryMock {

    public static List<TkmCardToken> getOnePageTkmCardToken() {
        TkmCardToken tkmCardToken = TkmCardToken.builder().htoken(Constant.HASH_1).build();
        TkmCardToken tkmCardToken2 = TkmCardToken.builder().htoken(Constant.HASH_2).build();
        return Arrays.asList(tkmCardToken, tkmCardToken2);
    }

    public static TkmCardToken getCardtoken() {
        return TkmCardToken.builder().htoken(Constant.HASH_1).build();
    }
}
