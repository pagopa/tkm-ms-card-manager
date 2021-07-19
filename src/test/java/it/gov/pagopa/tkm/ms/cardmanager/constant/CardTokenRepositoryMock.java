package it.gov.pagopa.tkm.ms.cardmanager.constant;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardTokenSubSet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CardTokenRepositoryMock {

    public static List<TkmCardTokenSubSet> getTkmCardTokensList() {
        return Arrays.asList(getTkmCardTokenSubSet(1, Constant.HASH_1), getTkmCardTokenSubSet(2, Constant.HASH_2));

    }

    @NotNull
    private static TkmCardTokenSubSet getTkmCardTokenSubSet(long id, String hash) {
        return new TkmCardTokenSubSet() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public String getHtoken() {
                return hash;
            }
        };
    }

    public static TkmCardToken getCardtoken() {
        return TkmCardToken.builder().htoken(Constant.HASH_1).build();
    }
}
