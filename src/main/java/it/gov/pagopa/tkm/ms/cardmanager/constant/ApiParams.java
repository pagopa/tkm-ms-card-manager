package it.gov.pagopa.tkm.ms.cardmanager.constant;

public class ApiParams {

    private ApiParams() {
    }

    //Consent Manager
    public static final String TAX_CODE_HEADER = "Tax-Code";
    public static final String HPAN_QUERY_PARAM = "hpan";
    public static final String SERVICES_QUERY_PARAM = "services";
    public static final String OCP_APIM_SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";

    // Card Manager
    public static final String MAX_NUMBER_OF_CARDS_PARAM = "maxNumberOfCards";
    public static final String MAX_NUMBER_OF_RECORDS_PARAM = "maxNumberOfRecord";
    public static final String PAGE_NUMBER_PARAM = "pageNumber";
    public static final String TOTAL_NUMBER_PAGES_HEADER = "Total-Number-Pages";
    public static final String REQUEST_ID_HEADER = "Request-Id";

}
