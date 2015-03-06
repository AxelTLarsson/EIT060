import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public final class ChallengeGenerator {
    private static final SecureRandom random = new SecureRandom();
    private static String nonce;
    
    /**
     * Generates challenge to be sent to client* 
     * @return the nonce
     */
    public static String getChallenge() {
        ChallengeGenerator.nonce = new BigInteger(130, random).toString();
        return nonce;
    }
    
    public static String getExpectedResponse(User user) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 1000;
        char[] chars = (nonce + user.getPassword()).toCharArray();
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
