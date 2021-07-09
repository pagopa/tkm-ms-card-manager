package it.gov.pagopa.tkm.ms.cardmanager.constant;

public class ApiParams {

    private ApiParams() {
    }

    // Consent Manager
    public static final String TAX_CODE_HEADER = "Tax-Code";
    public static final String HPAN_QUERY_PARAM = "hpan";
    public static final String SERVICES_QUERY_PARAM = "services";
    public static final String OCP_APIM_SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";

    // Card Manager
    public static final String MAX_NUMBER_OF_CARDS_PARAM = "maxNumberOfCards";
    public static final String MAX_NUMBER_OF_RECORDS_PARAM = "maxNumberOfRecords";
    public static final String HPAN_OFFSET_PARAM = "hpanOffset";
    public static final String HTOKEN_OFFSET_PARAM = "htokenOffset";
    public static final String REQUEST_ID_HEADER = "Request-Id";

    // Kafka queue
    public static final String FROM_ISSUER_HEADER = "From-Issuer";

}
