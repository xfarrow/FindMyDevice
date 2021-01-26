package de.nulide.findmydevice.utils;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptUtils {

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(12));
    }

    public static boolean checkPasswordHash(String hash, String password) {
        return BCrypt.checkpw(password, hash);
    }

}
