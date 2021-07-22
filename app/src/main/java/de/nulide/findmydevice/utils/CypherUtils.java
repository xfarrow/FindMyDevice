package de.nulide.findmydevice.utils;

import android.os.Build;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import de.nulide.findmydevice.data.Keys;

public class CypherUtils {

    private static final int IV_SIZE = 128;
    private static final int IV_LENGTH = IV_SIZE / 4;
    private static int keySize = 256;
    private static int iterationCount = 1867;
    private static int saltLength = keySize /4;

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(12));
    }

    public static boolean checkPasswordHash(String hash, String password) {
        return BCrypt.checkpw(password, hash);
    }

    public static String hashWithPKBDF2(String password){
        try {
            String salt = toHex(generateRandom(keySize / 8));
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), fromHex("cafe"), iterationCount*2, keySize);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return toHex(factory.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Keys genKeys(String password){
        KeyPair keypair = genKeyPair();
        Keys keys = new Keys(keypair.getPublic(), encryptKey(keypair.getPrivate(), password));
        return keys;
    }

    protected static KeyPair genKeyPair(){
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(1024, random);
            KeyPair pair = keyGen.generateKeyPair();
            return pair;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encryptWithKey(PublicKey pub, String msg){
        final Cipher rsa;
        try {
            rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsa.init(Cipher.ENCRYPT_MODE, pub);
            return rsa.doFinal(msg.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String decryptWithKey(PrivateKey priv, byte[] encryptedMsg){
        final Cipher rsa;
        try {
            rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsa.init(Cipher.DECRYPT_MODE, priv);
            return new String(rsa.doFinal(encryptedMsg), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String encryptKey(PrivateKey priv, String password){
        StringWriter sw = new StringWriter();
        PEMWriter writer = new PEMWriter(sw);
        PemObject po = new PemObject("RSA PRIVATE KEY", priv.getEncoded());
        try {
            writer.writeObject(po);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String pem = sw.getBuffer().toString();
        return encryptWithAES(pem.getBytes(), password);
    }

    public static PrivateKey decryptKey(String encryptedPrivKey, String password){
        try {
            String encodedKey = new String(decryptWithAES(encryptedPrivKey, password));
            byte[] key = decodeBase64(encodedKey);
            EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(privKeySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static String encryptWithAES(byte[] msg, String password){
        try {
            String salt = toHex(generateRandom(keySize / 8));
            String iv = toHex(generateRandom(IV_SIZE / 8));
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), fromHex(salt), iterationCount, keySize);
            SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(fromHex(iv)));
            byte[] encrypted = cipher.doFinal(msg);
            String encryptedBase64 = encodeBase64(encrypted);
            return salt + iv + encryptedBase64;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static byte[] decryptWithAES(String encryptedMsg, String password){
        try {
            String salt = encryptedMsg.substring(0, saltLength);
            String iv = encryptedMsg.substring(saltLength, saltLength + IV_LENGTH);
            String ct = encryptedMsg.substring(saltLength + IV_LENGTH);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), fromHex(salt), iterationCount, keySize);
            SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            byte[] encrypted = decodeBase64(ct);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(fromHex(iv)));
            byte[] decrypted = cipher.doFinal(encrypted);
            return decrypted;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeBase64(byte[] encoded){
        return DatatypeConverter.printBase64Binary(encoded);
    }

    public static byte[] decodeBase64(String encoded){
        return DatatypeConverter.parseBase64Binary(encoded);
    }

    private static byte[] fromHex(String str) {
        return DatatypeConverter.parseHexBinary(str);
    }

    private static String toHex(byte[] ba) {
        return DatatypeConverter.printHexBinary(ba);
    }

    private static byte[] generateRandom(int length) {
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[length];
        random.nextBytes(randomBytes);
        return randomBytes;
    }

}
