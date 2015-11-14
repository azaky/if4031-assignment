package if4031.mongodb.twitterclient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Toshiba on 11/14/2015.
 */
public class Utils {

    private Utils () {
        // Utility class
    }

    public static String hash(String s) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return s;
        }
        return byteArrayToHexString(md.digest(s.getBytes()));
    }

    public static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

}
