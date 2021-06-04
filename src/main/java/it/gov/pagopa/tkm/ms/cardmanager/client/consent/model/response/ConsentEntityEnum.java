package it.gov.pagopa.tkm.ms.cardmanager.client.consent.model.response;

public enum ConsentEntityEnum {

    Deny,
    Allow,
    Partial;

    public static ConsentEntityEnum toConsentEntityEnum(ConsentRequestEnum requestEnum) {
        return ConsentEntityEnum.valueOf(requestEnum.name());
    }

}
