package com.pentasecurity.core;

import com.pentasecurity.core.crypto.ECDSA;
import com.pentasecurity.core.utils.CryptoUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class ECDSATest {
    private String testPrivateKey = "269d46c9cfafe86be88fea3887422b520f7a9e8db829c2f8582200806e8d337a";
    private String testPublicKey = "04b8b6eae6da8a3eb2391064e2aa12a532fc3b2e2c5b37f258ea3c83f4905131b57cc4e4864adf68a71bfee34420388a8d12eb9d17f053a74f164523cc8c085e80";

    @Test
    public void generateKeyPairTest() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException, InvalidKeySpecException {
        KeyPair keyPair = ECDSA.generateKeyPair();

        BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
        BCECPublicKey publicKeyExpected = (BCECPublicKey) keyPair.getPublic();

        byte[] publicKey65BytesExpected = ECDSA.convertPubicKeyTo65Bytes(publicKeyExpected);

        BCECPublicKey publicKey = (BCECPublicKey) ECDSA.getPublicKeyFromPrivateKey(privateKey);
        byte[] publicKey65Bytes = ECDSA.convertPubicKeyTo65Bytes(publicKey);

        assertThat(CryptoUtils.bytesToHex(publicKey65Bytes), is(CryptoUtils.bytesToHex(publicKey65BytesExpected)));
    }

    @Test
    public void getPrivateKeyTest() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException, InvalidKeySpecException {
        KeyPair keyPair = ECDSA.generateKeyPair();

        BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
        BCECPrivateKey privateKeyNew = ECDSA.getPrivateKey(privateKey.getEncoded());

        byte[] privateKey32Bytes = ECDSA.convertPrivateKeyTo32Bytes(privateKeyNew);

        System.out.println("Private Key Hex: " + CryptoUtils.bytesToHex(privateKey32Bytes));
    }

    @Test
    public void getPublicKeyTest() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException, InvalidKeySpecException {
        KeyPair keyPair = ECDSA.generateKeyPair();

        BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();

        byte[] publicKeyBytes = publicKey.getEncoded();
        BCECPublicKey publicKeyNew = (BCECPublicKey) ECDSA.getPublicKey(publicKeyBytes);
        byte[] publicKey65Bytes = ECDSA.convertPubicKeyTo65Bytes(publicKeyNew);

        System.out.println("Public Key Hex: " + CryptoUtils.bytesToHex(publicKey65Bytes));
    }

    @Test
    public void getPrivateKeyFromHexStringTest() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchProviderException {
        BCECPrivateKey privateKey = (BCECPrivateKey) ECDSA.getPrivateKeyFromHexString(testPrivateKey);

        BCECPrivateKey privateKeyNew = ECDSA.getPrivateKey(privateKey.getEncoded());

        byte[] privateKey32Bytes = ECDSA.convertPrivateKeyTo32Bytes(privateKeyNew);

        assertThat(CryptoUtils.bytesToHex(privateKey32Bytes), is(testPrivateKey));
    }

    @Test
    public void getPublicKeyFromPrivateKeyTest() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException {
        BCECPrivateKey privateKey = (BCECPrivateKey) ECDSA.getPrivateKeyFromHexString(testPrivateKey);
        BCECPublicKey publicKey = (BCECPublicKey) ECDSA.getPublicKeyFromPrivateKey(privateKey);

        BCECPublicKey publicKeyNew = (BCECPublicKey) ECDSA.getPublicKey(publicKey.getEncoded());

        byte[] publicKey65Bytes = ECDSA.convertPubicKeyTo65Bytes(publicKeyNew);


        assertThat(CryptoUtils.bytesToHex(publicKey65Bytes), is(testPublicKey));
    }

    @Test
    public void getAddressFromPublicKeyTest() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        BCECPrivateKey privateKey = (BCECPrivateKey) ECDSA.getPrivateKeyFromHexString(testPrivateKey);
        BCECPublicKey publicKey = (BCECPublicKey) ECDSA.getPublicKeyFromPrivateKey(privateKey);

        BCECPublicKey publicKeyNew = (BCECPublicKey) ECDSA.getPublicKey(publicKey.getEncoded());

        String address = ECDSA.getAddressFromPublicKey(publicKeyNew);
        System.out.println(address);
    }

    @Test
    public void getSignatureTest() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException, NoSuchProviderException {
        BCECPrivateKey privateKey = (BCECPrivateKey) ECDSA.getPrivateKeyFromHexString(testPrivateKey);
        BCECPrivateKey privateKeyNew = ECDSA.getPrivateKey(privateKey.getEncoded());

        BCECPublicKey publicKey = (BCECPublicKey) ECDSA.getPublicKeyFromPrivateKey(privateKey);
        BCECPublicKey publicKeyNew = (BCECPublicKey) ECDSA.getPublicKey(publicKey.getEncoded());

        String msg = "Hello";

        byte[] signature = ECDSA.getSignature(privateKeyNew, msg);
        boolean result = ECDSA.verifySignature(signature, publicKeyNew, msg);

        assertThat(result, is(true));
    }
}
