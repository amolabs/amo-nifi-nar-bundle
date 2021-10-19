package com.pentasecurity.core.crypto;

import com.pentasecurity.core.utils.CryptoUtils;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.*;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

@Slf4j
public class ECDSA {
    private static ECNamedCurveParameterSpec SPEC = ECNamedCurveTable.getParameterSpec("secp256r1");
    static {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static KeyPair generateKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDSA", "BC");
        generator.initialize(SPEC, new SecureRandom());
        KeyPair keyPair = generator.generateKeyPair();
        return keyPair;
    }

    public static PrivateKey generateECDSAPrivateKey(byte[] privateKeyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory kf = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
        ECNamedCurveSpec params = new ECNamedCurveSpec("secp256r1", SPEC.getCurve(), SPEC.getG(), SPEC.getN());
        java.security.spec.ECPrivateKeySpec privKeySpec = new java.security.spec.ECPrivateKeySpec(new BigInteger(privateKeyBytes), params);
        return kf.generatePrivate(privKeySpec);
    }

    public static PublicKey generateECDSAPublicKey(byte[] publicKeyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256r1");
        KeyFactory kf = KeyFactory.getInstance("ECDSA", "BC");
        ECNamedCurveSpec params = new ECNamedCurveSpec("secp256r1", spec.getCurve(), spec.getG(), spec.getN());
        java.security.spec.ECPoint point = ECPointUtil.decodePoint(params.getCurve(), publicKeyBytes);
        java.security.spec.ECPublicKeySpec pubKeySpec = new java.security.spec.ECPublicKeySpec(point, params);
        return kf.generatePublic(pubKeySpec);
    }

    public static BCECPrivateKey getPrivateKey(byte[] privKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privKey);
        KeyFactory kf = KeyFactory.getInstance("ECDSA", "BC");
        BCECPrivateKey privateKey = (BCECPrivateKey) kf.generatePrivate(privateKeySpec);
        return privateKey;
    }

    public static PublicKey getPublicKey(byte[] pubKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pubKey);
        KeyFactory kf = KeyFactory.getInstance("ECDSA", "BC");
        PublicKey pub = kf.generatePublic(publicKeySpec);
        return pub;
    }

    public static byte[] convertPrivateKeyTo32Bytes(BCECPrivateKey privateKey) throws IOException {
        PrivateKeyInfo info = PrivateKeyInfo.getInstance (DERSequence.getInstance(privateKey.getEncoded()));
        DEROctetString raw1 = (DEROctetString)( DERSequence.getInstance(info.parsePrivateKey()) ).getObjectAt(1);

        return raw1.getOctets();
    }

    public static byte[] convertPubicKeyTo65Bytes(BCECPublicKey publicKey) throws IOException {
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        return publicKeyInfo.getPublicKeyData().getBytes();
    }

    public static PrivateKey getPrivateKeyFromHexString(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        BigInteger priv = new BigInteger(privateKey, 16);
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(priv, SPEC);
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");

        return keyFactory.generatePrivate(privateKeySpec);
    }

    public static PublicKey getPublicKeyFromPrivateKey(ECPrivateKey privateKey) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
        ECPoint Q = SPEC.getG().multiply(privateKey.getD());
        byte[] publicDerBytes = Q.getEncoded(false);

        ECPoint point = SPEC.getCurve().decodePoint(publicDerBytes);
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, SPEC);
        PublicKey publicKey = keyFactory.generatePublic(pubSpec);

        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        KeyFactory kf = KeyFactory.getInstance("ECDSA", "BC");
        PublicKey pub = kf.generatePublic(publicKeySpec);
        return pub;
    }

    public static byte[] getPrivateKey32Bytes(String privateKeyString) throws InvalidKeySpecException,
            NoSuchAlgorithmException, IOException, NoSuchProviderException {
        BCECPrivateKey privateKey = (BCECPrivateKey) getPrivateKeyFromHexString(privateKeyString);
        BCECPrivateKey privateKeyNew = getPrivateKey(privateKey.getEncoded());

        byte[] privateKey32Bytes = convertPrivateKeyTo32Bytes(privateKeyNew);

        return privateKey32Bytes;
    }

    public static byte[] getPublicKey65Bytes(String privateKeyString) throws InvalidKeySpecException,
            NoSuchAlgorithmException, IOException, NoSuchProviderException {
        BCECPrivateKey privateKey =
                (BCECPrivateKey) getPrivateKeyFromHexString(privateKeyString);
        BCECPublicKey publicKey = (BCECPublicKey) getPublicKeyFromPrivateKey(privateKey);
        BCECPublicKey publicKeyNew = (BCECPublicKey) getPublicKey(publicKey.getEncoded());

        byte[] publicKey65Bytes = convertPubicKeyTo65Bytes(publicKeyNew);

        return publicKey65Bytes;
    }

    public static String getAddressFromPublicKey(BCECPublicKey publicKey) throws NoSuchAlgorithmException, IOException {
        byte[] publicKey65Bytes = ECDSA.convertPubicKeyTo65Bytes(publicKey);
        byte[] sha256 = CryptoUtils.sha256(publicKey65Bytes);
        byte[] slice = Arrays.copyOf(sha256, 20);
        String address = CryptoUtils.bytesToHex(slice);

        return address.toUpperCase();
    }

    public static String getAddressFromPrivateKeyString(String privateKeyString) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeySpecException, IOException {
        BCECPrivateKey privateKey = (BCECPrivateKey) getPrivateKeyFromHexString(privateKeyString);
        BCECPublicKey publicKey = (BCECPublicKey) getPublicKeyFromPrivateKey(privateKey);
        BCECPublicKey publicKeyNew = (BCECPublicKey) getPublicKey(publicKey.getEncoded());

        byte[] publicKey65Bytes = ECDSA.convertPubicKeyTo65Bytes(publicKey);
        byte[] sha256 = CryptoUtils.sha256(publicKey65Bytes);
        byte[] slice = Arrays.copyOf(sha256, 20);
        String address = CryptoUtils.bytesToHex(slice);

        return address.toUpperCase();
    }

    public static byte[] getSignature(ECPrivateKey privateKey, String msg)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        signature.update(msgBytes);
        return signature.sign();
    }

    public static boolean verifySignature(byte[] signature, ECPublicKey publicKey, String msg)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature ecdsa = Signature.getInstance("SHA256withECDSA");
        ecdsa.initVerify(publicKey);
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        ecdsa.update(msgBytes);
        return ecdsa.verify(signature);
    }

    public static byte[] sign(PrivateKey privKey, byte[] data) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature ecdsa = Signature.getInstance("SHA256withECDSA");
        ecdsa.initSign(privKey);
        ecdsa.update(data);
        log.info("#### tx bytes lentgh : " + data.length);
        return SignDataASN1Parse(ecdsa.sign());
    }

    public static byte[] compressedToUncompressed(byte[] compKey) {
        if (compKey[0] == 0x04) {
            return compKey;
        }
        org.bouncycastle.math.ec.ECPoint point = SPEC.getCurve().decodePoint(compKey);
        byte[] x = point.getXCoord().getEncoded();
        byte[] y = point.getYCoord().getEncoded();
        // concat 0x04, x, and y, make sure x and y has 32-bytes:
        return concat(new byte[]{0x04}, x, y);
    }

    private static byte[] SignDataASN1Parse(byte[] data) throws IOException {
        ASN1InputStream asn1_is = new ASN1InputStream(data);
        DLSequence dlSeq = (DLSequence) asn1_is.readObject();
        ASN1Integer asn1_R = (ASN1Integer) dlSeq.getObjectAt(0);
        ASN1Integer asn1_S = (ASN1Integer) dlSeq.getObjectAt(1);
        asn1_is.close();

        byte[] byte_R = asn1_R.getValue().toByteArray();
        byte[] byte_S = asn1_S.getValue().toByteArray();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        log.info("#### ECDSA > SignDataASN1Parse : byte_R length : " + byte_R.length);
        log.info("#### ECDSA > SignDataASN1Parse : byte_S length : " + byte_S.length);

        checkByteRS(byte_R, bos);
        checkByteRS(byte_S, bos);

        return bos.toByteArray();
    }

    private static void checkByteRS(byte[] byte_RS, ByteArrayOutputStream bos) throws IOException {
        if (byte_RS.length > 32) {
            bos.write(byte_RS, 1, 32);
        } else if (byte_RS.length == 31) {
            bos.write(new byte[]{0});
            bos.write(byte_RS, 0, 31);
        } else {
            bos.write(byte_RS, 0, 32);
        }
    }

    private static byte[] concat(byte[]... ins) {
        int pos = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte[] in : ins) {
            baos.write(in, 0, in.length);
        }
        return baos.toByteArray();
    }
}
