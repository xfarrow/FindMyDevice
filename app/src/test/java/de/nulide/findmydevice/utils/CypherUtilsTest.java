package de.nulide.findmydevice.utils;

import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPair;
import java.security.PrivateKey;

import static org.junit.Assert.assertEquals;

public class CypherUtilsTest {

    @Test
    public void testKeyEncryption(){
        KeyPair keys = CypherUtils.genKeyPair();
        String msg = "The password is *****";
        byte[] encryptedMsg = CypherUtils.encryptWithKey(keys.getPublic(), msg);
        String decryptedMsg = CypherUtils.decryptWithKey(keys.getPrivate(), encryptedMsg);
        assertEquals(decryptedMsg, msg);
        Assert.assertEquals(msg, decryptedMsg);
    }


    @Test
    public void testBase64(){
        KeyPair keys = CypherUtils.genKeyPair();
        PrivateKey priv = keys.getPrivate();
        byte[] encoded = priv.getEncoded();
        String stringed = CypherUtils.encodeBase64(encoded);
        byte[] encodedString = CypherUtils.decodeBase64(stringed);
        String reencoded = CypherUtils.encodeBase64(encodedString);
        Assert.assertEquals(stringed, reencoded);
    }

    @Test
    public void testKeysGen(){
        CypherUtils.genKeys("test");
    }


    @Test
    public void testAESEncryption(){
        String msg = "Another msg";
        String password = "secure";
        String encryptedMsg = CypherUtils.encryptWithAES(msg.getBytes(), password);
        String decryptedMsg = new String(CypherUtils.decryptWithAES(encryptedMsg, password));
        Assert.assertEquals(msg, new String(decryptedMsg));
    }

    @Test
    public void testHashWithPKBDF2(){
        String password = "honey";
        String hashedOne = CypherUtils.hashWithPKBDF2(password);
        String hashedTwo = CypherUtils.hashWithPKBDF2(password);
        Assert.assertEquals(hashedOne, hashedTwo);
    }

}
