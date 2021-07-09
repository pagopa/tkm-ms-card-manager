package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import it.gov.pagopa.tkm.annotation.EnableExecutionTime;
import it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum;
import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.service.CryptoService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import reactor.netty.resources.ConnectionProvider;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Service
@Log4j2
@EnableExecutionTime
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
        ConnectionProvider connectionProvider = ConnectionProvider.create("MyProvider", 100);
        Duration duration = Duration.ofMillis(2000);
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().connectionProvider(connectionProvider).readTimeout(duration).responseTimeout(duration).build();
        cryptoClient = new CryptographyClientBuilder()
                .keyIdentifier(key.getId())
                .httpClient(httpClient)
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
    public String encryptNullable(String toEncrypt) {
        if (StringUtils.isBlank(toEncrypt)) {
            return null;
        }
        return encrypt(toEncrypt);
    }

}
