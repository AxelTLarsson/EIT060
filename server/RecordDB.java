import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;


public class RecordDB implements Serializable{
	private HashMap<Integer, MedRecord> records;
	
	public RecordDB() {
		this.records = new HashMap<Integer, MedRecord>();
	}
	
	public MedRecord getRecord(Integer persNbr) {
		return records.get(persNbr);
	}
	
	public void addRecord(Integer persNbr, MedRecord medRecord) {
		records.put(persNbr, medRecord);
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
	
	public RecordDB loadFromDisk() {
        try
        {
            FileInputStream fileIn = new FileInputStream("records");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            records = (HashMap<Integer, MedRecord>) in.readObject();
            in.close();
            fileIn.close();
        } catch(IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } 
        if (records == null) {
        	records = new HashMap<Integer, MedRecord>();
        	this.saveToDisk();
        }
        return this;
	}
	
}


