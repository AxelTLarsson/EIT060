import java.io.Serializable;

/**
 * Created by axel on 02/03/15.
 */
public class User implements Serializable {
    private String name;
    private Enum position;
    private String password; // Yes, that is right we are storing the password in clear text, but secure room
    private int IDnumber;
    private String type;
    private String division;
    
    public static String DR = "DR";
    public static String Nurse = "NURSE";
    public static String Patient = "PATIENT";
    public static String Gov = "GOV";
    
    public User(String name, String type, String password, int IDnumber, String division) { //Enum position
        this.name = name;
        this.position = position;
        this.type = type;
        this.password = password;
        this.IDnumber = IDnumber;
        this.division = division;
    }
    
    @Override
    public String toString() {
        return name + " (" + IDnumber +") " + type + " : " + password;
    }
    public int getID(){
    	return IDnumber;
    }
    
    public String getName() {
    	return name;
    }
    
    public String getDivision() {
    	return division;
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