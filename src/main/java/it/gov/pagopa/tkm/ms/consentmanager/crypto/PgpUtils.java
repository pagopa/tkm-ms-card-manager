package it.gov.pagopa.tkm.ms.consentmanager.crypto;

import io.micrometer.core.instrument.util.*;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.jce.provider.*;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.bouncycastle.util.io.Streams;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.annotation.*;
import java.io.*;
import java.security.SecureRandom;
import java.util.*;

@Service
public class PgpUtils {

    private PgpUtils() {
    }

    @Value("${keyvault.readQueuePrvPgpKey}")
    private String privateKey;

    @Value("${keyvault.readQueuePubPgpKey}")
    private String publicKeyFromKeyVault;

    private PGPPublicKey publicKey;

    private final BouncyCastleProvider provider = new BouncyCastleProvider();

    @PostConstruct
    public void init() throws Exception {
        publicKey = readPublicKey(new ByteArrayInputStream(publicKeyFromKeyVault.getBytes()));
    }

    public byte[] encrypt(byte[] message, boolean armored) throws PGPException {
        try {
            final ByteArrayInputStream in = new ByteArrayInputStream(message);
            final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            final PGPLiteralDataGenerator literal = new PGPLiteralDataGenerator();
            final PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZIP);
            final OutputStream pOut =
                    literal.open(comData.open(bOut), PGPLiteralData.BINARY, "filename", in.available(), new Date());
            Streams.pipeAll(in, pOut);
            comData.close();
            final byte[] bytes = bOut.toByteArray();
            final PGPEncryptedDataGenerator generator = new PGPEncryptedDataGenerator(
                    new JcePGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_256).setWithIntegrityPacket(true)
                            .setSecureRandom(
                                    new SecureRandom())

           );
            generator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKey));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStream theOut = armored ? new ArmoredOutputStream(out) : out;
            OutputStream cOut = generator.open(theOut, bytes.length);
            cOut.write(bytes);
            cOut.close();
            theOut.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new PGPException("Error in encrypt", e);
        }
    }

    public PGPPublicKey readPublicKey(InputStream in) throws IOException, PGPException {
        PGPPublicKeyRingCollection keyRingCollection = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(in), new JcaKeyFingerprintCalculator());
        PGPPublicKey publicKey = null;
        Iterator<PGPPublicKeyRing> rIt = keyRingCollection.getKeyRings();
        while (publicKey == null && rIt.hasNext()) {
            PGPPublicKeyRing kRing = rIt.next();
            Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
            while (publicKey == null && kIt.hasNext()) {
                PGPPublicKey key = kIt.next();
                if (key.isEncryptionKey()) {
                    publicKey = key;
                }
            }
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("Can't find public key in the key ring.");
        }
        return publicKey;
    }

    public String decrypt(String encryptedMessage) throws Exception {
        if (StringUtils.isBlank(encryptedMessage)) {
            return null;
        }
        byte[] encryptedMessageBytes = encryptedMessage.getBytes();
        byte[] privateKeyBytes = privateKey.getBytes();
        PGPObjectFactory encryptedObjectFactory = new PGPObjectFactory(PGPUtil.getDecoderStream(new ByteArrayInputStream(encryptedMessageBytes)), new JcaKeyFingerprintCalculator());
        PGPEncryptedDataList pgpEncryptedDataList;
        Object object = encryptedObjectFactory.nextObject();
        if (object instanceof PGPEncryptedDataList) {
            pgpEncryptedDataList = (PGPEncryptedDataList) object;
        } else {
            pgpEncryptedDataList = (PGPEncryptedDataList) encryptedObjectFactory.nextObject();
        }
        Iterator iterator = pgpEncryptedDataList.getEncryptedDataObjects();
        PGPPrivateKey pgpPrivateKey = null;
        PGPPublicKeyEncryptedData publicKeyEncryptedData = null;
        PGPSecretKeyRingCollection secretKeyRingCollection = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(privateKeyBytes)), new JcaKeyFingerprintCalculator());
        while (pgpPrivateKey == null && iterator.hasNext()) {
            publicKeyEncryptedData = (PGPPublicKeyEncryptedData) iterator.next();
            PGPSecretKey secretKey = secretKeyRingCollection.getSecretKey(publicKeyEncryptedData.getKeyID());
            if (secretKey != null) {
                //TODO: ADD PASSPHRASE?
                pgpPrivateKey = secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider(provider).build(null));
            }
        }
        if (pgpPrivateKey == null) {
            throw new IllegalArgumentException("Secret key for message not found.");
        }
        InputStream decryptedMessageStream = publicKeyEncryptedData.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider(provider).build(pgpPrivateKey));
        PGPObjectFactory decryptedObjectFactory = new PGPObjectFactory(decryptedMessageStream, new JcaKeyFingerprintCalculator());
        Object nextObject = decryptedObjectFactory.nextObject();
        if (nextObject instanceof PGPCompressedData) {
            PGPCompressedData compressedData = (PGPCompressedData) nextObject;
            nextObject = new PGPObjectFactory(PGPUtil.getDecoderStream(compressedData.getDataStream()), new JcaKeyFingerprintCalculator()).nextObject();
        } else if (!(nextObject instanceof PGPLiteralData)) {
            throw new IllegalArgumentException("Object cannot be cast to PGPCompressedData or PGPLiteralData");
        }
        PGPLiteralData literalData = (PGPLiteralData) nextObject;
        InputStream literalDataStream = literalData.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int ch;
        while ((ch = literalDataStream.read()) >= 0) {
            outputStream.write(ch);
        }
        outputStream.close();
        return outputStream.toString();
    }

}

