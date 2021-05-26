package it.gov.pagopa.tkm.ms.cardmanager.constant;

import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.*;

import java.time.*;
import java.util.*;

public class DefaultBeans {

    public final String TAX_CODE_1 = "PCCRLE04M24L219D";
    public final String TAX_CODE_2 = "TRRCLE04M24L219D";
    public final String PAN_1 = "111111111111";
    public final String PAN_2 = "222222222222";
    public final String PAR_1 = "abc11111111111";
    public final String PAR_2 = "cba222222222222";
    public final String HPAN = "92fc472e8709cf61aa2b6f8bb9cf61aa2b6f8bd8267f9c14f58f59cf61aa2b6f";
    public final String TOKEN_1 = "abcde123";
    public final String TOKEN_2 = "xyz6543";
    public final String HTOKEN_1 = "12fc472e8709cf61aa2b6f8bb9cf61aa2b6f8bd8267f9c14f58f59cf61aa2b6a";
    public final String HTOKEN_2 = "22fc472e8709cf61aa2b6f8bb9cf61aa2b6f8bd8267f9c14f58f59cf61aa2b6b";

    public final Instant INSTANT = Instant.parse("2018-11-30T18:35:24.00Z");

    public final Set<String> TOKEN_LIST = new HashSet<>(Arrays.asList(TOKEN_1, TOKEN_2));

    public final ParlessCardResponse PARLESS_CARD_1 = new ParlessCardResponse(TAX_CODE_1, PAN_1, TOKEN_LIST, CircuitEnum.AMEX);
    public final ParlessCardResponse PARLESS_CARD_2 = new ParlessCardResponse(TAX_CODE_2, PAN_2, TOKEN_LIST, CircuitEnum.VISA);
    public final List<ParlessCardResponse> PARLESS_CARD_LIST = Arrays.asList(PARLESS_CARD_1, PARLESS_CARD_2);

    public final TkmCardToken TKM_CARD_TOKEN_1 = new TkmCardToken()
            .setToken(TOKEN_1)
            .setHtoken(HTOKEN_1);
    public final TkmCardToken TKM_CARD_TOKEN_2 = new TkmCardToken()
            .setToken(TOKEN_2)
            .setHtoken(HTOKEN_2);
    public  final Set<TkmCardToken> TKM_CARD_TOKENS = new HashSet<>(Arrays.asList(TKM_CARD_TOKEN_1, TKM_CARD_TOKEN_2));

    public final TkmCard TKM_CARD_1 = new TkmCard()
            .setCircuit(CircuitEnum.AMEX)
            .setHpan(HPAN)
            .setPan(PAN_1)
            .setPar(PAR_1)
            .setTaxCode(TAX_CODE_1)
            .setTokens(TKM_CARD_TOKENS);
    public final TkmCard TKM_CARD_2 = new TkmCard()
            .setCircuit(CircuitEnum.VISA)
            .setHpan(HPAN)
            .setPan(PAN_2)
            .setPar(PAR_2)
            .setTaxCode(TAX_CODE_2)
            .setTokens(TKM_CARD_TOKENS);

    public final List<TkmCard> TKM_CARD_LIST = Arrays.asList(TKM_CARD_1, TKM_CARD_2);

}
