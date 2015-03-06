import java.io.Serializable;

public class User implements Serializable {
    private final String name;
    private Enum position;
    private final String password; // Yes, that is right we are storing the password in clear text, but secure room
    private final String IDnumber;
    private final String type;
    private final String division;

    public static final String DR = "DR";
    public static final String NURSE = "NURSE";
    public static final String PATIENT = "PATIENT";
    public static final String GOV = "GOV";
    
    public User(String name, String type, String password, String IDnumber, String division) { //Enum position
        this.name = name;
        this.position = position; // Vad händer här? Detta är ju helt galet eller?
        this.type = type;
        this.password = password;
        this.IDnumber = IDnumber;
        this.division = division;
    }

    @Override
    public String toString() {
        return name + " (" + IDnumber +") " + type + " : " + password;
    }
    public String getID(){
    	return IDnumber;
    }
    
    public String getName() {
    	return name;
    }
    
    public String getDivision() {
    	return division;
    }

    public String getPassword() {
        return password;
    }

    public enum Position {
        DR,
        NURSE,
        PATIENT,
        GOV
    }
    
    public String getPosition(){
    	return type; //TODO: Not using enums.
    }
    
}