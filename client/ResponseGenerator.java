import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class ResponseGenerator {
    
    /**
     * Generate response to server challenge, asks
     * user for password.
     */
    public static String getResponse(String nonce) throws InvalidKeySpecException, NoSuchAlgorithmException {
        int iterations = 1000;
        char[] chars = (nonce + new String(Utils.readPassword("Password for server authentication please: "))).toCharArray();
        byte[] salt = "salt".getBytes(); // Does not matter, since we are on an SSL connection and not actually storing the
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = secretKeyFactory.generateSecret(spec).getEncoded();
        return stringToHex(hash);
        
    }

    private static String stringToHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }
}
