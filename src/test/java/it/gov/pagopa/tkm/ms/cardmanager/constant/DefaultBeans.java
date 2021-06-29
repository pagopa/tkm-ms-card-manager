package it.gov.pagopa.tkm.ms.cardmanager.constant;

import com.google.common.collect.Sets;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueue;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.ReadQueueToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.*;

import static it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentEntityEnum.Partial;

public class DefaultBeans {

    public DefaultBeans() {
    }

    public static String enc(String toEnc) {
        return "ENC_" + toEnc;
    }

    public static TkmCard encCard(TkmCard card) {
        card.setPan(enc(card.getPan()));
        for (TkmCardToken token : card.getTokens()) {
            token.setToken(enc(token.getToken()));
        }
        return card;
    }

    public static String dec(String toDec) {
        return "DEC_" + toDec;
    }

    public static ParlessCardResponse decCard(ParlessCardResponse card) {
        card.setPan(dec(card.getPan()));
        card.setTokens(card.getTokens().stream().map(t -> t = new ParlessCardToken(dec(t.getToken()), t.getHtoken())).collect(Collectors.toSet()));
        return card;
    }

    public static final Instant INSTANT = Instant.MAX;
    public final String TAX_CODE_1 = "PCCRLE04M24L219D";
    private final String TAX_CODE_2 = "TRRCLE04M24L219D";
    public final String PAN_1 = "111111111111";
    public final String PAN_2 = "222222222222";
    public final String PAR_1 = "abc11111111111";
    private final String PAR_2 = "cba222222222222";
    public final String HPAN_1 = "92fc472e8709cf61aa2b6f8bb9cf61aa2b6f8bd8267f9c14f58f59cf61aa2b6f";
    public final String TOKEN_1 = "abcde123";
    public final String TOKEN_2 = "xyz6543";
    public final String TOKEN_3 = "aerr126";
    private final String HTOKEN_1 = "12fc472e8709cf61aa2b6f8bb9cf61aa2b6f8bd8267f9c14f58f59cf61aa2b6a";
    private final String HTOKEN_2 = "22fc472e8709cf61aa2b6f8bb9cf61aa2b6f8bd8267f9c14f58f59cf61aa2b6b";
    private final String HTOKEN_3 = "32fc472e8709cf61aa2b6f8bb9cf61aa2b6f8bd8267f9c14f58f59cf61aa2b6c";

    private final Set<ParlessCardToken> PARLESS_CARD_TOKENS = new HashSet<>(Arrays.asList(
            new ParlessCardToken(TOKEN_1, HTOKEN_1),
            new ParlessCardToken(TOKEN_2, HTOKEN_2)
    ));

    private final ParlessCardResponse PARLESS_CARD_1 = new ParlessCardResponse(PAN_1, HPAN_1, CircuitEnum.AMEX, PARLESS_CARD_TOKENS);
    private final ParlessCardResponse PARLESS_CARD_2 = new ParlessCardResponse(PAN_2, HPAN_1, CircuitEnum.VISA, PARLESS_CARD_TOKENS);
    public final List<ParlessCardResponse> PARLESS_CARD_LIST = Arrays.asList(decCard(PARLESS_CARD_1), decCard(PARLESS_CARD_2));

    public final TkmCardToken TKM_CARD_TOKEN_1 = TkmCardToken.builder()
            .token(TOKEN_1)
            .htoken(HTOKEN_1)
            .build();
    public final TkmCardToken TKM_CARD_TOKEN_2 = TkmCardToken.builder()
            .token(TOKEN_2)
            .htoken(HTOKEN_2)
            .build();
    public final TkmCardToken TKM_CARD_TOKEN_3 = TkmCardToken.builder()
            .token(TOKEN_3)
            .htoken(HTOKEN_3)
            .build();
    private final Set<TkmCardToken> TKM_CARD_TOKENS_1 = new HashSet<>(Arrays.asList(TKM_CARD_TOKEN_1, TKM_CARD_TOKEN_2));

    public final TkmCard TKM_CARD_PAN_PAR_1 = TkmCard.builder()
            .circuit(CircuitEnum.AMEX)
            .hpan(HPAN_1)
            .pan(PAN_1)
            .par(PAR_1)
//            .taxCode(TAX_CODE_1) todo
            .tokens(TKM_CARD_TOKENS_1)
            .build();
    public final TkmCard TKM_CARD_PAN_1 = TkmCard.builder()
            .circuit(CircuitEnum.AMEX)
            .hpan(HPAN_1)
            .pan(PAN_1)
//            .taxCode(TAX_CODE_1)todo
            .tokens(TKM_CARD_TOKENS_1)
            .build();
    public final TkmCard TKM_CARD_PAR_1 = TkmCard.builder()
            .circuit(CircuitEnum.AMEX)
            .par(PAR_1)
//            .taxCode(TAX_CODE_1) todo
            .tokens(TKM_CARD_TOKENS_1)
            .build();
    private final TkmCard TKM_CARD_PAN_PAR_2 = TkmCard.builder()
            .circuit(CircuitEnum.VISA)
            .hpan(HPAN_1)
            .pan(PAN_2)
            .par(PAR_2)
//            .taxCode(TAX_CODE_2)todo
            .tokens(TKM_CARD_TOKENS_1)
            .build();

    public final List<TkmCard> TKM_CARD_LIST = Arrays.asList(TKM_CARD_PAN_PAR_1, TKM_CARD_PAN_PAR_2);

    private final ReadQueueToken QUEUE_TOKEN_1 = new ReadQueueToken(TOKEN_1, HTOKEN_1);
    private final ReadQueueToken QUEUE_TOKEN_2 = new ReadQueueToken(TOKEN_2, HTOKEN_2);
    private final ReadQueueToken QUEUE_TOKEN_3 = new ReadQueueToken(TOKEN_3, HTOKEN_3);
    private final List<ReadQueueToken> QUEUE_TOKEN_LIST_1 = Arrays.asList(QUEUE_TOKEN_1, QUEUE_TOKEN_2);
    public final List<ReadQueueToken> QUEUE_TOKEN_LIST_2 = Arrays.asList(QUEUE_TOKEN_1, QUEUE_TOKEN_3);

    public final ReadQueue READ_QUEUE_PAN_PAR_1 = new ReadQueue(
            TAX_CODE_1,
            PAN_1,
            HPAN_1,
            PAR_1,
            CircuitEnum.AMEX,
            QUEUE_TOKEN_LIST_1);
    public final ReadQueue READ_QUEUE_PAN_PAR_2 = new ReadQueue(
            TAX_CODE_1,
            PAN_1,
            HPAN_1,
            PAR_1,
            CircuitEnum.AMEX,
            QUEUE_TOKEN_LIST_2);
    public final ReadQueue READ_QUEUE_PAN_1 = new ReadQueue(
            TAX_CODE_1,
            PAN_1,
            HPAN_1,
            null,
            CircuitEnum.AMEX,
            QUEUE_TOKEN_LIST_1);
    public final ReadQueue READ_QUEUE_PAR_1 = new ReadQueue(
            TAX_CODE_1,
            null,
            null,
            PAR_1,
            CircuitEnum.AMEX,
            QUEUE_TOKEN_LIST_1);

    private final Set<WriteQueueToken> WRITE_QUEUE_TOKENS_NEW = new HashSet<>(Arrays.asList(
            new WriteQueueToken(
                    HTOKEN_1,
                    TokenActionEnum.INSERT_UPDATE
            ),
            new WriteQueueToken(
                    HTOKEN_2,
                    TokenActionEnum.INSERT_UPDATE
            )
    ));

    private final Set<WriteQueueToken> WRITE_QUEUE_TOKENS_UPDATED = new HashSet<>(Arrays.asList(
            new WriteQueueToken(
                    HTOKEN_2,
                    TokenActionEnum.DELETE
            ),
            new WriteQueueToken(
                    HTOKEN_3,
                    TokenActionEnum.INSERT_UPDATE
            )
    ));

    private final Set<WriteQueueCard> WRITE_QUEUE_CARD_NEW = Collections.singleton(new WriteQueueCard(
            HPAN_1,
            CardActionEnum.INSERT_UPDATE,
            PAR_1,
            WRITE_QUEUE_TOKENS_NEW
    ));

    private final Set<WriteQueueCard> WRITE_QUEUE_CARD_UPDATED = Collections.singleton(new WriteQueueCard(
            HPAN_1,
            CardActionEnum.INSERT_UPDATE,
            PAR_1,
            WRITE_QUEUE_TOKENS_UPDATED
    ));

    public final Set<WriteQueueCard> WRITE_QUEUE_CARD_REVOKED_CONSENT = Collections.singleton(new WriteQueueCard(
            HPAN_1,
            CardActionEnum.REVOKE,
            PAR_1,
            null
    ));

    public final WriteQueue WRITE_QUEUE_FOR_NEW_CARD = new WriteQueue(
            TAX_CODE_1,
            INSTANT,
            WRITE_QUEUE_CARD_NEW
    );

    public final WriteQueue WRITE_QUEUE_FOR_UPDATED_CARD = new WriteQueue(
            TAX_CODE_1,
            INSTANT,
            WRITE_QUEUE_CARD_UPDATED
    );

    public final WriteQueue WRITE_QUEUE_FOR_REVOKED_CONSENT_CARD = new WriteQueue(
            TAX_CODE_1,
            INSTANT,
            WRITE_QUEUE_CARD_REVOKED_CONSENT
    );

    public ConsentResponse getConsentUpdateGlobal(ConsentEntityEnum consentEntityEnum) {
        return ConsentResponse.builder()
                .consent(consentEntityEnum)
                .taxCode(TAX_CODE_1)
                .build();
    }

    public ConsentResponse getConsentUpdatePartial() {
        return ConsentResponse.builder()
                .consent(Partial)
                .taxCode(TAX_CODE_1)
                .details(getCardServiceConsentSet())
                .build();
    }

    private Set<CardServiceConsent> getCardServiceConsentSet() {
        Set<CardServiceConsent> cardServiceConsentSet = Sets.newHashSet();
        cardServiceConsentSet.add(createCardServiceConsent());
        cardServiceConsentSet.add(createCardServiceConsentOnlyBpd());
        return cardServiceConsentSet;
    }

    private CardServiceConsent createCardServiceConsentOnlyBpd() {
        CardServiceConsent cardServiceConsent = new CardServiceConsent();
        cardServiceConsent.setHpan(HPAN_1);
        cardServiceConsent.setServiceConsents(createServiceConsentOnlyBpd());
        return cardServiceConsent;
    }

    private CardServiceConsent createCardServiceConsent() {
        CardServiceConsent cardServiceConsent = new CardServiceConsent();
        cardServiceConsent.setHpan(HPAN_1);
        cardServiceConsent.setServiceConsents(createServiceConsent());
        return cardServiceConsent;
    }

    private Set<ServiceConsent> createServiceConsentOnlyBpd() {
        Set<ServiceConsent> serviceConsentSet = Sets.newHashSet();
        serviceConsentSet.add(new ServiceConsent(ConsentRequestEnum.Allow, ServiceEnum.BPD));
        return serviceConsentSet;
    }

    private Set<ServiceConsent> createServiceConsent() {
        Set<ServiceConsent> serviceConsentSet = Sets.newHashSet();
        serviceConsentSet.add(new ServiceConsent(ConsentRequestEnum.Allow, ServiceEnum.BPD));
        serviceConsentSet.add(new ServiceConsent(ConsentRequestEnum.Deny, ServiceEnum.FA));
        return serviceConsentSet;
    }

}
