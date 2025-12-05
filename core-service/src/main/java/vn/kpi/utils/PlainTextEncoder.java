package vn.kpi.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PlainTextEncoder {
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String encode(final String plaintext, String salt) {

        String str = plaintext + salt;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256"); //step 2
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
        try {
            md.update(str.getBytes("UTF-8")); //step 3
        } catch (UnsupportedEncodingException e) {
        }
        byte[] raw = md.digest(); //step 4

        return getFormattedText(raw);
    }

    private static String getFormattedText(byte[] bytes) {
        final StringBuilder buf = new StringBuilder(bytes.length * 2);

        for (int j = 0; j < bytes.length; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

    public static void main(String[] args) {
        System.out.println(encode("123456", String.format("@#%d56", 1)));
    }
}
