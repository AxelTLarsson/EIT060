import java.util.HashMap;

/**
 * Created by axel on 02/03/15.
 * This class is responsible for authenticating users based on their
 */
public class Authenticator {
    
    public static String getRandomPassword() {
        return "password";
    }

    public static User authenticateUser(Integer subject, HashMap<Integer, User> users) {
        return users.get(subject);
    }
}
