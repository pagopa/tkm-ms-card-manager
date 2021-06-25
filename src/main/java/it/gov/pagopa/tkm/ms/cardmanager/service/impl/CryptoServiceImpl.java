package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.azure.identity.*;
import com.azure.security.keyvault.keys.*;
import com.azure.security.keyvault.keys.cryptography.*;
import com.azure.security.keyvault.keys.cryptography.models.*;
import com.azure.security.keyvault.keys.models.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.exception.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.CryptoService;
import lombok.extern.log4j.*;
import org.apache.commons.lang3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import javax.annotation.*;

@Service
@Log4j2
public class CryptoServiceImpl implements CryptoService {

    @Value("${azure.keyvault.uri}")
    private String keyvaultUri;

    @Value("${azure.keyvault.tenant-id}")
    private String tenantId;

    @Value("${azure.keyvault.client-id}")
    private String clientId;

    @Value("${azure.keyvault.client-key}")
    private String clientKey;

    @Value("${keyvault.cryptographicKeyId}")
    private String keyId;

    private CryptographyClient cryptoClient;

    @PostConstruct
    public void init() {
        final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientKey)
                .tenantId(tenantId)
                .build();
        final KeyClient keyClient = new KeyClientBuilder().vaultUrl(keyvaultUri).credential(clientSecretCredential).buildClient();
        final KeyVaultKey key = keyClient.getKey(keyId);
        final String keyId = key.getId();
        cryptoClient = new CryptographyClientBuilder()
                .keyIdentifier(keyId)
                .credential(clientSecretCredential)
                .buildClient();
    }

    @Override
    public String encrypt(String toEncrypt) {
        if (StringUtils.isBlank(toEncrypt)) {
            throw new CardException(ErrorCodeEnum.KEYVAULT_ENCRYPTION_FAILED);
        }
        EncryptResult enc = cryptoClient.encrypt(EncryptionAlgorithm.RSA_OAEP_256, toEncrypt.getBytes());
        if (enc == null || ArrayUtils.isEmpty(enc.getCipherText())) {
            throw new CardException(ErrorCodeEnum.KEYVAULT_ENCRYPTION_FAILED);
        }
        return Base64Utils.encodeToString(enc.getCipherText());
    }

    @Override
    public String decrypt(String toDecrypt) {
        if (StringUtils.isBlank(toDecrypt)) {
            throw new CardException(ErrorCodeEnum.KEYVAULT_DECRYPTION_FAILED);
        }
        DecryptResult dec = cryptoClient.decrypt(EncryptionAlgorithm.RSA_OAEP_256, Base64Utils.decodeFromString(toDecrypt));
        if (dec == null || ArrayUtils.isEmpty(dec.getPlainText())) {
            throw new CardException(ErrorCodeEnum.KEYVAULT_DECRYPTION_FAILED);
        }
        return new String(dec.getPlainText());
    }

}
