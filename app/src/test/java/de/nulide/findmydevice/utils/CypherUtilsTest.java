package de.nulide.findmydevice.utils;

import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPair;
import java.security.PrivateKey;

import static org.junit.Assert.assertEquals;

public class CypherUtilsTest {

    @Test
    public void testKeyEncryption(){
        KeyPair keys = CypherUtils.genKeys();
        String msg = "The password is *****";
        byte[] encryptedMsg = CypherUtils.encryptWithKey(keys.getPublic(), msg);
        String decryptedMsg = CypherUtils.decryptWithKey(keys.getPrivate(), encryptedMsg);
        assertEquals(decryptedMsg, msg);
        Assert.assertEquals(msg, decryptedMsg);
    }

    @Test
    public void testAESEncryption(){
        String msg = "Another msg";
        String password = "secure";
        byte[] encryptedMsg = CypherUtils.encryptWithAES(msg.getBytes(), password);
        byte[] decryptedMsg = CypherUtils.decryptWithAES(encryptedMsg, password);
        Assert.assertEquals(msg, new String(decryptedMsg));
    }

    @Test
    public void testPrivateKeyEncryption(){
        KeyPair keys = CypherUtils.genKeys();
        String password = "Snake";
        byte[] encodedPrivKey = keys.getPrivate().getEncoded();
        byte[] encryptedPrivKey = CypherUtils.encryptKey(keys.getPrivate(), password);
        PrivateKey decryptedKey = CypherUtils.decryptKey(encryptedPrivKey, password);
        byte[] encodedDecryptedKey = decryptedKey.getEncoded();
        for(int i=0; i< encodedPrivKey.length; i++){
            Assert.assertEquals(encodedPrivKey[i], encodedDecryptedKey[i]);
        }
    }

}
