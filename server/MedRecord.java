import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MedRecord implements Serializable{
	private final String patient;
	private final String doctor;
	private final String nurse;
	private final String division;
	private String information;
	private String patientID, doctorID, nurseID;

	public MedRecord(String patient, String patientID, String doctor, String doctorID, String nurse, String nurseID, String division, String text) {
		this.patient = patient;
		this.patientID = patientID;
		this.doctor = doctor;
		this.doctorID = doctorID;
		this.nurse = nurse;
		this.nurseID = nurseID;
		this.division = division;
		this.information = "";
		append(text);
	}
	
	public String getDocID() {
		return doctorID;
	}
	
	public String getNurseID() {
		return nurseID;
	}
	
	public String getPatientID() {
		return patientID;
	}
	
	public String getDivision() {
		return division;
	}
	
	public String getDoctor() {
		return doctor;
	}
	
	public String getNurse() {
		return nurse;
	}
	
	public String getPatient() {
		return patient;
	}
	
	public void append(String text) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		
		information = information + "\n" + dateFormat.format(date) + "\n" + text + "\n";
	}
	
	public String toString() {
		return patient + "\n" + doctor + "\n" + nurse + "\n" + division + "\n" + information + "\n";
		
	}
}
