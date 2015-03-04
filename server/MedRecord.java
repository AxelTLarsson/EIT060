import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MedRecord implements Serializable{
	private String patient;
	private String doctor;
	private String nurse;
	private String division;
	private String information;

	public MedRecord(String patient, String doctor, String nurse, String division, String text) {
		this.patient = patient;
		this.doctor = doctor;
		this.nurse = nurse;
		this.division = division;		
		this.information = text;
	}
	
	public void append(String text) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		
		information = information + dateFormat.format(date) + "\n" + text + "\n\n";
	}
	
	public String toString() {
		return patient + "\n" + doctor + "\n" + nurse + "\n" + division + "\n" + information + "\n";
		
	}
}
