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
	private int patientID, doctorID, nurseID;

	public MedRecord(String patient, int patientID, String doctor, int doctorID, String nurse, int nurseID, String division, String text) {
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
	
	public int getDocID() {
		return doctorID;
	}
	
	public int getNurseID() {
		return nurseID;
	}
	
	public int getPatientID() {
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
