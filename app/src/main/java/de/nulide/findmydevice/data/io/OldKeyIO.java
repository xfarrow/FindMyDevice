package de.nulide.findmydevice.data.io;

import static de.nulide.findmydevice.data.io.IO.context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import de.nulide.findmydevice.data.Keys;

public class OldKeyIO {

    public final static String pubKeyFile = "pub.key";
    public final static String encPrivKeyFile = "priv.key";

    public final static String hashedPWFile = "hashedPW";

    public static void writeKeys(Keys keys) {
        File pubFile = new File(context.getFilesDir(), pubKeyFile);
        File privFile = new File(context.getFilesDir(), encPrivKeyFile);
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
            fos.write(keys.getEncryptedPrivateKey().getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Keys readKeys(){
        File pubFile = new File(context.getFilesDir(), pubKeyFile);
        File privFile = new File(context.getFilesDir(), encPrivKeyFile);

        EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(readByteArray(pubFile));
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

        return new Keys(publicKey, new String(encPrivateKey));
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

    public static void writeHashedPassword(String hashedPW) {
        File file = new File(context.getFilesDir(), hashedPWFile);
        try {
            PrintWriter out = new PrintWriter(file);
            out.write(hashedPW);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String readHashedPW() {
        File file = new File(context.getFilesDir(), hashedPWFile);
        if(file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                StringBuilder pw = new StringBuilder();
                try {
                    String line;

                    while ((line = br.readLine()) != null) {
                        pw.append(line);
                    }
                    br.close();
                    return pw.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
