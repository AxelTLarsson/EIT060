/**
 * Created by axel on 04/03/15.
 */
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class ChallengeGenerator {
    private static SecureRandom random = new SecureRandom();
    public static String nonce;
    
    /**
     * Generates challenge to be sent to client* 
     * @return the nonce
     */
    public static String getChallenge() {
        ChallengeGenerator.nonce = new BigInteger(130, random).toString();
        System.out.println("Challenge generated: " + nonce);
        return nonce;
    }
    
    public static String getExpectedResponse(User user) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 1000;
        char[] chars = (nonce + user.getPassword()).toCharArray();
        byte[] salt = "salt".getBytes(); // Does not matter, since we are on an SSL connection and not actually storing the 
        System.out.println("byte[] salt: " + Arrays.toString(salt));
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = secretKeyFactory.generateSecret(spec).getEncoded();
        String expectedResponse = stringToHex(hash);
        
        System.out.println("Expected response is: " + expectedResponse + " Generated with nonce: " + nonce );
        return expectedResponse;
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
