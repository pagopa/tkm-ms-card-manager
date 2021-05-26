package it.gov.pagopa.tkm.ms.cardmanager.constant;

import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.*;

import java.time.*;
import java.util.*;

public class DefaultBeans {

    public final String TAX_CODE_1 = "PCCRLE04M24L219D";
    public final String TAX_CODE_2 = "TRRCLE04M24L219D";
    public final String PAN_1 = "111111111111";
    public final String PAN_2 = "222222222222";
    public final String HPAN = "92fc472e8709cf61aa2b6f8bb9cf61aa2b6f8bd8267f9c14f58f59cf61aa2b6f";
    public final String TOKEN_1 = "abcde123";
    public final String TOKEN_2 = "xyz6543";

    public final Instant INSTANT = Instant.parse("2018-08-19T16:45:42.00Z");

    public final Set<String> TOKEN_LIST = new HashSet<>(Arrays.asList(TOKEN_1, TOKEN_2));

    public final ParlessCardResponse PARLESS_CARD_1 = new ParlessCardResponse(TAX_CODE_1, PAN_1, TOKEN_LIST, CircuitEnum.AMEX);
    public final ParlessCardResponse PARLESS_CARD_2 = new ParlessCardResponse(TAX_CODE_2, PAN_2, TOKEN_LIST, CircuitEnum.VISA);
    public final List<ParlessCardResponse> PARLESS_CARD_LIST = Arrays.asList(PARLESS_CARD_1, PARLESS_CARD_2);

}
