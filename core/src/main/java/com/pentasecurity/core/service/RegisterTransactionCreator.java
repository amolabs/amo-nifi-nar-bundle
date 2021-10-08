package com.pentasecurity.core.service;

import com.google.gson.Gson;
import com.pentasecurity.core.crypto.ECDSA;
import com.pentasecurity.core.dto.Payload;
import com.pentasecurity.core.dto.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class RegisterTransactionCreator extends TransactionCreator implements TransactionCreatable{
    @Override
    public Transaction createTransaction() {
        return null;
    }

    @Override
    protected Transaction createSignedTx() {
        return null;
    }

    @Override
    protected Transaction createUnsignedTx() {
        return null;
    }

    public enum TxType {
        //coins and stakes
        transfer,
        stake,
        withdraw,
        delegate,
        retract,

        //governance
        propose,
        vote,

        //storage
        setup,
        close,

        //parcels
        register,
        request,
        grant,
        discard,
        cancel,
        revoke,

        // user-defined coin
        issue,
        burn,
        lock,
    }


    private static byte[] sha256(String msg) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(msg.getBytes());

        return md.digest();
    }

    private static byte[] sha256(byte[] msg) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(msg);

        return md.digest();
    }

    public Transaction createRegisterTx(String sender, BigInteger fee, int last_height, String parcelId, String custody, String proxy_account, String extraInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("parcelId", parcelId);


        if (StringUtils.isNotBlank(proxy_account)) {
            map.put("proxy_account", proxy_account);
        }
        if (StringUtils.isNotBlank(custody)) {
            map.put("custody", custody);
        }
        if (StringUtils.isNotBlank(extraInfo)) {
            map.put("extraInfo", extraInfo);
        }
        return createTx(TxType.register, sender, fee, last_height, map);
    }

    public Transaction setupTx(
            String sender, BigInteger fee, int lastHeight,
            int storageID, String url, String registrationFee, String hostingFee) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("registrationFee", registrationFee);
        map.put("hostingFee", hostingFee);
        map.put("storageID", storageID);
        map.put("url", url);

        return createTx(TxType.setup, sender, fee, lastHeight, map);
    }

    public Transaction closeStorageTx(String sender, BigInteger fee, int lastHeight, int storageID) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("storageID", storageID);

        return createTx(TxType.close, sender, fee, lastHeight, map);
    }

    public Transaction stakeTx(String sender, BigInteger fee, int last_height, String validator, String amount) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("validator", validator);
        map.put("amount", amount);

        return createTx(TxType.stake, sender, fee, last_height, map);
    }

    public Transaction transferTx(String sender, BigInteger fee, int last_height, String to, String amount) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("to", to);
        map.put("amount", amount);

        return createTx(TxType.transfer, sender, fee, last_height, map);
    }

    private Transaction createTx(TxType type, String sender, BigInteger fee, int last_height, Map<String, Object> map) {
        Gson gson = new Gson();
        Transaction amoTx = new Transaction(type, sender, fee.toString(), String.valueOf(last_height));
        Payload payload = new Payload();
        switch (type) {
            case register:
                payload.setTarget((String) map.get("parcelId"));
                payload.setCustody((String) map.get("custody"));

                if (map.containsKey("proxy_account")) {
                    payload.setProxyAccount("");
                }

                if (map.containsKey("extraInfo")) {
                    String extraJson = (String) map.get("extraInfo");
                    Map extra = gson.fromJson(extraJson, Map.class);
                    payload.setExtra(extra);
                }
                break;
            case stake:
                payload.setValidator((String) map.get("validator"));
                payload.setAmount((String) map.get("amount"));
                break;
            case transfer:
                payload.setTo((String) map.get("to"));
                payload.setAmount((String) map.get("amount"));
                break;
            case setup:
                payload.setStorage((Integer) map.get("storageID")); // storage 값은 쌍따옴표를 붙이면 안됨
                payload.setUrl((String) map.get("url"));
                payload.setRegistrationFee((String) map.get("registrationFee"));
                payload.setHostingFee((String) map.get("hostingFee"));
                break;
            case close:
                payload.setStorage((Integer) map.get("storageID"));
                break;
            default:
                break;
        }
        amoTx.setPayload(payload);
        log.info("#### unsigned tx : " + gson.toJson(amoTx));
        return amoTx;
    }

/*
    public PrivateKey generatePrivateKey(byte[] keyBin) throws InvalidKeySpecException, NoSuchAlgorithmException {
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("P-256");
        KeyFactory kf = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
        ECNamedCurveSpec params = new ECNamedCurveSpec("P-256", spec.getCurve(), spec.getG(), spec.getN());
        ECPrivateKeySpec privKeySpec = new ECPrivateKeySpec(new BigInteger(keyBin), params);
        return kf.generatePrivate(privKeySpec);
    }
*/

    public static ECPrivateKey getPrivateKey(byte[] privateKeyBytes) {
        try {

            //AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            //parameters.init(new ECGenParameterSpec("P-256"));
            Security.addProvider(new BouncyCastleProvider());
            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256r1");
            //ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);
            ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(new BigInteger(privateKeyBytes), spec);

            ECPrivateKey privateKey = (ECPrivateKey) KeyFactory.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME).generatePrivate(ecPrivateKeySpec);

            return privateKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            System.out.println(e.getClass().getSimpleName() + " occurred when trying to get private key from raw bytes");
            e.printStackTrace();

            return null;
        }
    }

    public byte[] sign(byte[] privKey, byte[] data) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, InvalidParameterSpecException, IOException {
        ECPrivateKey ecPrivateKey = (ECPrivateKey) ECDSA.generateECDSAPrivateKey(privKey);
        log.info("#### Transaction > sign() > tx length: " + data.length);
        return ECDSA.sign(ecPrivateKey, data);
//
//        ECPrivateKey  privateKey = getPrivateKey(privKey);
//        Signature ecdsa = Signature.getInstance("SHA256withECDSA");
//        ecdsa.initSign(privateKey);
//        ecdsa.update(data);
//        System.out.println(Hex.toHexString(privateKey.getEncoded()));

//        return ecdsa.sign();
    }

/*
    public byte[] verify(byte[] publicKey, byte[] data) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, InvalidParameterSpecException, NoSuchProviderException {
        byte[] uncompressedPubkey = compressedToUncompressed(publicKey);
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey);


        ECPublicKeySpec pubKey = new ECPublicKeySpec(
                curve.decodePoint(Hex.decode("025b6dc53bc61a2548ffb0f671472de6c9521a9d2d2534e65abfcbd5fe0c70")), // Q
                spec);
        KeyFactory kf = KeyFactory.getInstance("ECDSA", "BC");
        PublicKey           vKey = kf.generatePublic(publicKey);

        Signature ecdsa = Signature.getInstance("SHA256withECDSA");
        ecdsa.initVerify();
        ecdsa.update(data);
        return ecdsa.verify();
    }

*/

    public String create(byte[] privKey, byte[] pubKey, Transaction amoTx) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, InvalidParameterSpecException, IOException {
        Gson gson = new Gson();
        com.pentasecurity.core.dto.Signature signature = new com.pentasecurity.core.dto.Signature();

        byte[] uncompressedPubkey = ECDSA.compressedToUncompressed(pubKey);
        //byte[] hash = sha256(tx);
        byte[] sign = sign(privKey, gson.toJson(amoTx).getBytes());

        signature.setPubkey(Hex.toHexString(uncompressedPubkey).toUpperCase());
        signature.setSigBytes(Hex.toHexString(sign).toUpperCase());

        amoTx.setSignature(signature);

        log.info("#### signed.tx.json : " + gson.toJson(amoTx));
        return Base64.toBase64String(gson.toJson(amoTx).getBytes());
    }
}
