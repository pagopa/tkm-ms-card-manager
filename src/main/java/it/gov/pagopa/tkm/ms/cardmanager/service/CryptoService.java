package it.gov.pagopa.tkm.ms.cardmanager.service;

public interface CryptoService {

    String encrypt(String toEncrypt);

    String decrypt(String toDecrypt);

}
