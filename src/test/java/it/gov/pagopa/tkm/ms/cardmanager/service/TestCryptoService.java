//package it.gov.pagopa.tkm.ms.cardmanager.service;
//
//import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
//import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
//import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
//import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
//import it.gov.pagopa.tkm.ms.cardmanager.service.impl.CryptoServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.KEYVAULT_ENCRYPTION_FAILED;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.when;
//
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@ExtendWith(MockitoExtension.class)
//class TestCryptoService {
//
//    @InjectMocks
//    private CryptoServiceImpl cryptoService;
//
//    @Mock
//    private CryptographyClient client;
//
//    @BeforeEach
//    void init() {
//        ReflectionTestUtils.setField(cryptoService, "keyvaultUri", "http://127.0.0.1");
//        ReflectionTestUtils.setField(cryptoService, "tenantId", "TENANT_ID");
//        ReflectionTestUtils.setField(cryptoService, "clientId", "CLIENT_ID");
//        ReflectionTestUtils.setField(cryptoService, "clientKey", "CLIENT_KEY");
//        ReflectionTestUtils.setField(cryptoService, "keyId", "KEY_ID");
//    }
//
//    @Test
//    void givenPlaintextString_returnEncrypted() {
//        EncryptResult enc = new EncryptResult("ENCRYPTED".getBytes(), EncryptionAlgorithm.RSA_OAEP_256, "KEY_ID");
//        when(client.encrypt(EncryptionAlgorithm.RSA_OAEP_256, "PLAINTEXT".getBytes())).thenReturn(enc);
//        assertEquals("RU5DUllQVEVE", cryptoService.encrypt("PLAINTEXT"));
//    }
//
//    @Test
//    void encryptNullable_returnEncryptedNotNull() {
//        EncryptResult enc = new EncryptResult("ENCRYPTED".getBytes(), EncryptionAlgorithm.RSA_OAEP_256, "KEY_ID");
//        when(client.encrypt(EncryptionAlgorithm.RSA_OAEP_256, "PLAINTEXT".getBytes())).thenReturn(enc);
//        assertEquals("RU5DUllQVEVE", cryptoService.encryptNullable("PLAINTEXT"));
//    }
//
//    @Test
//    void encryptNullable_returnEncryptedNull() {
//        assertNull(cryptoService.encryptNullable(""));
//        Mockito.verify(client, never()).encrypt(Mockito.any(EncryptionAlgorithm.class), Mockito.any());
//    }
//
//    @Test
//    void givenEmptyStringToEncrypt_throwException() {
//        CardException cardException = assertThrows(CardException.class, () -> cryptoService.encrypt(""));
//        assertEquals(KEYVAULT_ENCRYPTION_FAILED, cardException.getErrorCode());
//    }
//
//    @Test
//    void givenEmptyEncryptResponse_throwException() {
//        EncryptResult enc = new EncryptResult("".getBytes(), EncryptionAlgorithm.RSA_OAEP_256, "KEY_ID");
//        when(client.encrypt(EncryptionAlgorithm.RSA_OAEP_256, "PLAINTEXT".getBytes())).thenReturn(enc);
//        CardException cardException = assertThrows(CardException.class, () -> cryptoService.encrypt("PLAINTEXT"));
//        assertEquals(KEYVAULT_ENCRYPTION_FAILED, cardException.getErrorCode());
//    }
//
//}
