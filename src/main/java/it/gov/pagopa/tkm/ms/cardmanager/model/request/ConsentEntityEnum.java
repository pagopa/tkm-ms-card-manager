package it.gov.pagopa.tkm.ms.cardmanager.model.request;

public enum ConsentEntityEnum {

    Deny,
    Allow,
    Partial;

    public static ConsentEntityEnum toConsentEntityEnum(ConsentRequestEnum requestEnum) {
        return ConsentEntityEnum.valueOf(requestEnum.name());
    }

}
