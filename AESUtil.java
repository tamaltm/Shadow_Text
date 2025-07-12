import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    public static String encrypt(String data, String password) throws Exception {
        SecretKeySpec key = getKey(password);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedData, String password) throws Exception {
        SecretKeySpec key = getKey(password);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, "UTF-8");
    }

    private static SecretKeySpec getKey(String password) throws Exception {
        byte[] key = new byte[16];
        byte[] passwordBytes = password.getBytes("UTF-8");
        System.arraycopy(passwordBytes, 0, key, 0, Math.min(passwordBytes.length, key.length));
        return new SecretKeySpec(key, "AES");
    }
}
