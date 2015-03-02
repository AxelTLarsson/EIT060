import java.io.Serializable;

/**
 * Created by axel on 02/03/15.
 */
public class User implements Serializable {
    private String name;
    private Enum position;
    private String password; // Yes, that is right we are storing the password in clear text, but secure room

    
    public User(String name, Enum position, String password) {
        this.name = name;
        this.position = position;
        this.password = password;
    }
    
    @Override
    public String toString() {
        return position + " " + name + " : " + password;
    }
    
    public enum Position {
        DR,
        NURSE,
        PATIENT,
        GOV
    }
}
