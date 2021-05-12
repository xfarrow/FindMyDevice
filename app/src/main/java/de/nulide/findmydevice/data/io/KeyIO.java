package de.nulide.findmydevice.data.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import de.nulide.findmydevice.data.Keys;

public class KeyIO {

    public final static String pubKeyFile = "pub.key";
    public final static String encPrivKeyFile = "priv.key";

    public void saveKeys(Keys keys){
        File pubFile = new File(IO.context.getFilesDir(), pubKeyFile);
        File privFile = new File(IO.context.getFilesDir(), encPrivKeyFile);
        try {
            if (!pubFile.exists()) {
                pubFile.createNewFile();
            }else{
                pubFile.delete();
                pubFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(pubFile);
            fos.write(keys.getPublicKey().getEncoded());
            fos.close();
            if (!privFile.exists()) {
                privFile.createNewFile();
            }else{
                privFile.delete();
                privFile.createNewFile();
            }
            fos = new FileOutputStream(privFile);
            fos.write(keys.getEncryptedPrivateKey());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Keys readKeys(){
        File pubFile = new File(IO.context.getFilesDir(), pubKeyFile);
        File privFile = new File(IO.context.getFilesDir(), encPrivKeyFile);

        EncodedKeySpec pubKeySpec = new PKCS8EncodedKeySpec(readByteArray(pubFile));
        PublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(pubKeySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        byte[] encPrivateKey = readByteArray(privFile);

        return new Keys(publicKey, encPrivateKey);
    }


    private static byte[] readByteArray(File file)
    {
        FileInputStream fileInputStream = null;
        byte[] data = new byte[(int) file.length()];
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(data);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
