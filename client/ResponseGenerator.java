/**
 * Created by axel on 04/03/15.
 */

public class ResponseGenerator {
    
    /**
     * Generate response to server challenge, asks
     * user for password.
     * @param challenge the challenge from the server
     * @return the correct response
     */
    public static String getResponse(String challenge) {
        char[] password = Utils.readPassword("Password for server authentication please: ");
        return "response" + password;
        
    }
}
