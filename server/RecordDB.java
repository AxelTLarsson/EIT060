import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;


public class RecordDB implements Serializable{
	private HashMap<String, MedRecord> records;

	public RecordDB() {
		this.records = new HashMap<String, MedRecord>();
	}
	
	public MedRecord getRecord(String persNbr) {
		//logga
		return records.get(persNbr);
	}
	
	public void addRecord(String persNbr, MedRecord medRecord) {
		//logga
		records.put(persNbr, medRecord);
		this.saveToDisk();
	}
	
	public void deleteRecord(String persNbr) {
		//logga
		records.remove(persNbr);
		this.saveToDisk();
	}
	
	public void saveToDisk() {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream("records");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(records);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data (med records) is saved in records\n");
        } catch (IOException i) {
            i.printStackTrace();
        }
		
	}

    @SuppressWarnings("unchecked")
	public RecordDB loadFromDisk() {
        try
        {
            FileInputStream fileIn = new FileInputStream("records");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            records = (HashMap<String, MedRecord>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            System.err.println("Could not find file \"records\", a new one will be created.");
        } catch (ClassNotFoundException e) { // Probably happens when upgrading
            System.err.println("Could not load file \"records\", delete the file and start over.");
        }
        if (records == null) {
        	records = new HashMap<String, MedRecord>();
        	this.saveToDisk();
        }
        return this;
	}
	
}


