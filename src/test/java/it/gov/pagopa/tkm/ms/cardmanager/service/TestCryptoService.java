package it.gov.pagopa.tkm.ms.cardmanager.service;

import com.azure.security.keyvault.keys.cryptography.*;
import com.azure.security.keyvault.keys.cryptography.models.*;
import it.gov.pagopa.tkm.ms.cardmanager.exception.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.test.util.*;
import org.springframework.util.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.KEYVAULT_ENCRYPTION_FAILED;
import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.KEYVAULT_DECRYPTION_FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class TestCryptoService {

    @InjectMocks
    private CryptoServiceImpl cryptoService;

    @Mock
    private CryptographyClient client;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(cryptoService, "keyvaultUri", "http://127.0.0.1");
        ReflectionTestUtils.setField(cryptoService, "tenantId", "TENANT_ID");
        ReflectionTestUtils.setField(cryptoService, "clientId", "CLIENT_ID");
        ReflectionTestUtils.setField(cryptoService, "clientKey", "CLIENT_KEY");
        ReflectionTestUtils.setField(cryptoService, "keyId", "KEY_ID");
    }

    @Test
    void givenPlaintextString_returnEncrypted() {
        EncryptResult enc = new EncryptResult("ENCRYPTED".getBytes(), EncryptionAlgorithm.RSA_OAEP_256, "KEY_ID");
        when(client.encrypt(EncryptionAlgorithm.RSA_OAEP_256, "PLAINTEXT".getBytes())).thenReturn(enc);
        assertEquals("RU5DUllQVEVE", cryptoService.encrypt("PLAINTEXT"));
    }

    @Test
    void givenEmptyStringToEncrypt_throwException() {
        CardException cardException = assertThrows(CardException.class, () -> cryptoService.encrypt(""));
        assertEquals(KEYVAULT_ENCRYPTION_FAILED, cardException.getErrorCode());
    }

    @Test
    void givenEmptyEncryptResponse_throwException() {
        EncryptResult enc = new EncryptResult("".getBytes(), EncryptionAlgorithm.RSA_OAEP_256, "KEY_ID");
        when(client.encrypt(EncryptionAlgorithm.RSA_OAEP_256, "PLAINTEXT".getBytes())).thenReturn(enc);
        CardException cardException = assertThrows(CardException.class, () -> cryptoService.encrypt("PLAINTEXT"));
        assertEquals(KEYVAULT_ENCRYPTION_FAILED, cardException.getErrorCode());
    }

    @Test
    void givenEncryptedString_returnPlaintext() {
        DecryptResult dec = new DecryptResult("PLAINTEXT".getBytes(), EncryptionAlgorithm.RSA_OAEP_256, "KEY_ID");
        when(client.decrypt(EncryptionAlgorithm.RSA_OAEP_256, Base64Utils.decodeFromString("RU5DUllQVEVE"))).thenReturn(dec);
        assertEquals("PLAINTEXT", cryptoService.decrypt("RU5DUllQVEVE"));
    }

    @Test
    void givenEmptyEncryptedString_throwException() {
        CardException cardException = assertThrows(CardException.class, () -> cryptoService.decrypt(""));
        assertEquals(KEYVAULT_DECRYPTION_FAILED, cardException.getErrorCode());
    }

    @Test
    void givenEmptyDecryptResponse_throwException() {
        DecryptResult dec = new DecryptResult("".getBytes(), EncryptionAlgorithm.RSA_OAEP_256, "KEY_ID");
        when(client.decrypt(EncryptionAlgorithm.RSA_OAEP_256, Base64Utils.decodeFromString("RU5DUllQVEVE"))).thenReturn(dec);
        CardException cardException = assertThrows(CardException.class, () -> cryptoService.decrypt("RU5DUllQVEVE"));
        assertEquals(KEYVAULT_DECRYPTION_FAILED, cardException.getErrorCode());
    }

}
