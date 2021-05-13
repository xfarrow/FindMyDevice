package de.nulide.findmydevice.utils;

import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPair;
import java.security.PrivateKey;

public class CypherUtilsTest {

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
}
