package it.gov.pagopa.tkm.ms.cardmanager.constant;

import com.google.common.collect.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.read.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.topic.write.*;

import java.time.*;
import java.util.*;

import static it.gov.pagopa.tkm.ms.cardmanager.model.request.ConsentEntityEnum.*;

public class DefaultBeans {

    public DefaultBeans() {
    }

    public final static Instant INSTANT = Instant.parse("2018-08-19T16:45:42.00Z");

    public final String TAX_CODE_1 = "PCCRLE04M24L219D";
    public final String TAX_CODE_2 = "TRRCLE04M24L219D";
    public final String PAN_1 = "111111111111";
    public final String PAN_2 = "222222222222";
    public final String PAR_1 = "abc11111111111";
    public final String PAR_2 = "cba222222222222";
    public final String HPAN_1 = "92fc472e8709cf61aa2b6f8bb9cf61aa2b6f8bd8267f9c14f58f59cf61aa2b6f";
    public final String TOKEN_1 = "abcde123";
    public final String TOKEN_2 = "xyz6543";
    public final String TOKEN_3 = "aerr126";
    public final String HTOKEN_1 = "12fc472e8709cf61aa2b6f8bb9cf61aa2b6f8bd8267f9c14f58f59cf61aa2b6a";
    public final String HTOKEN_2 = "22fc472e8709cf61aa2b6f8bb9cf61aa2b6f8bd8267f9c14f58f59cf61aa2b6b";
    public final String HTOKEN_3 = "32fc472e8709cf61aa2b6f8bb9cf61aa2b6f8bd8267f9c14f58f59cf61aa2b6c";

    public final Set<String> TOKEN_SET = new HashSet<>(Arrays.asList(TOKEN_1, TOKEN_2));

    public final ParlessCardResponse PARLESS_CARD_1 = new ParlessCardResponse(TAX_CODE_1, PAN_1, TOKEN_SET, CircuitEnum.AMEX);
    public final ParlessCardResponse PARLESS_CARD_2 = new ParlessCardResponse(TAX_CODE_2, PAN_2, TOKEN_SET, CircuitEnum.VISA);
    public final List<ParlessCardResponse> PARLESS_CARD_LIST = Arrays.asList(PARLESS_CARD_1, PARLESS_CARD_2);

    public final TkmCardToken TKM_CARD_TOKEN_1 = new TkmCardToken()
            .setToken(TOKEN_1)
            .setHtoken(HTOKEN_1);
    public final TkmCardToken TKM_CARD_TOKEN_2 = new TkmCardToken()
            .setToken(TOKEN_2)
            .setHtoken(HTOKEN_2);
    public final TkmCardToken TKM_CARD_TOKEN_3 = new TkmCardToken()
            .setToken(TOKEN_3)
            .setHtoken(HTOKEN_3);
    public final Set<TkmCardToken> TKM_CARD_TOKENS_1 = new HashSet<>(Arrays.asList(TKM_CARD_TOKEN_1, TKM_CARD_TOKEN_2));

    public final TkmCard TKM_CARD_PAN_PAR_1 = new TkmCard()
            .setCircuit(CircuitEnum.AMEX)
            .setHpan(HPAN_1)
            .setPan(PAN_1)
            .setPar(PAR_1)
            .setTaxCode(TAX_CODE_1)
            .setTokens(TKM_CARD_TOKENS_1);
    public final TkmCard TKM_CARD_PAN_1 = new TkmCard()
            .setCircuit(CircuitEnum.AMEX)
            .setHpan(HPAN_1)
            .setPan(PAN_1)
            .setTaxCode(TAX_CODE_1)
            .setTokens(TKM_CARD_TOKENS_1);
    public final TkmCard TKM_CARD_PAR_1 = new TkmCard()
            .setCircuit(CircuitEnum.AMEX)
            .setPar(PAR_1)
            .setTaxCode(TAX_CODE_1)
            .setTokens(TKM_CARD_TOKENS_1);
    public final TkmCard TKM_CARD_PAN_PAR_2 = new TkmCard()
            .setCircuit(CircuitEnum.VISA)
            .setHpan(HPAN_1)
            .setPan(PAN_2)
            .setPar(PAR_2)
            .setTaxCode(TAX_CODE_2)
            .setTokens(TKM_CARD_TOKENS_1);

    public final List<TkmCard> TKM_CARD_LIST = Arrays.asList(TKM_CARD_PAN_PAR_1, TKM_CARD_PAN_PAR_2);

    public final ReadQueueToken QUEUE_TOKEN_1 = new ReadQueueToken(TOKEN_1, HTOKEN_1);
    public final ReadQueueToken QUEUE_TOKEN_2 = new ReadQueueToken(TOKEN_2, HTOKEN_2);
    public final ReadQueueToken QUEUE_TOKEN_3 = new ReadQueueToken(TOKEN_3, HTOKEN_3);
    public final List<ReadQueueToken> QUEUE_TOKEN_LIST_1 = Arrays.asList(QUEUE_TOKEN_1, QUEUE_TOKEN_2);
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

    public final Set<WriteQueueToken> WRITE_QUEUE_TOKENS_NEW = new HashSet<>(Arrays.asList(
            new WriteQueueToken(
                    HTOKEN_1,
                    TokenActionEnum.INSERT_UPDATE
            ),
            new WriteQueueToken(
                    HTOKEN_2,
                    TokenActionEnum.INSERT_UPDATE
            )
    ));

    public final Set<WriteQueueToken> WRITE_QUEUE_TOKENS_UPDATED = new HashSet<>(Arrays.asList(
            new WriteQueueToken(
                    HTOKEN_2,
                    TokenActionEnum.DELETE
            ),
            new WriteQueueToken(
                    HTOKEN_3,
                    TokenActionEnum.INSERT_UPDATE
            )
    ));

    public final Set<WriteQueueCard> WRITE_QUEUE_CARD_NEW = Collections.singleton(new WriteQueueCard(
            HPAN_1,
            CardActionEnum.INSERT_UPDATE,
            PAR_1,
            WRITE_QUEUE_TOKENS_NEW
    ));

    public final Set<WriteQueueCard> WRITE_QUEUE_CARD_UPDATED = Collections.singleton(new WriteQueueCard(
            HPAN_1,
            CardActionEnum.INSERT_UPDATE,
            PAR_1,
            WRITE_QUEUE_TOKENS_UPDATED
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

    public ConsentResponse getConsentUpdateGlobal(ConsentEntityEnum consentEntityEnum) {
        return new ConsentResponse()
                .setConsent(consentEntityEnum)
                .setTaxCode(TAX_CODE_1);
    }

    public ConsentResponse getConsentUpdatePartial() {
        return new ConsentResponse()
                .setConsent(Partial)
                .setTaxCode(TAX_CODE_1)
                .setDetails(getCardServiceConsentSet());
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
