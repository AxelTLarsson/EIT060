import java.io.*;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

public class Server implements Runnable {
	private ServerSocket serverSocket = null;
	private static int numConnectedClients = 0;
	private static HashMap<String, User> users;
	private static RecordDB medRecords;
    private boolean authed;

	private Server(ServerSocket ss) {
		serverSocket = ss;
		newListener();
	}

	public void run() {
		try {
			SSLSocket socket = (SSLSocket) serverSocket.accept();
			newListener();
			SSLSession session = socket.getSession();
			X509Certificate cert = (X509Certificate) session
					.getPeerCertificateChain()[0];
			String issuer = cert.getIssuerDN().getName();
			String subject = cert.getSubjectDN().getName();
			String serial = cert.getSerialNumber().toString();
			subject = subject.substring(3); // CN=personNummer -> personNummer
			
            
			User user = Authenticator.authenticateUser(subject, users);
			if (user == null) {
                System.err.println("Could not authenticate " + subject);
                socket.close();
				return; // this connection is not authenticated
			}

            System.out.println("User: " + user.toString() + " logged in via certificates.");
            
            // Extra Challenge-Response
            String challenge = ChallengeGenerator.getChallenge();
            String expectedResponse = ChallengeGenerator.getExpectedResponse(user);
                       

            // Set up connection to client
			numConnectedClients++;
			PrintWriter out = null;
			BufferedReader in = null;
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
            
            // Send challenge
            System.out.println("Sending: challenge: " + challenge);
			out.println("challenge: " + challenge);
            out.flush();
            String response = null;
            while ((response = in.readLine()) != null && !authed) {
                if (response.equals(expectedResponse)) {
                    System.out.println("Client response: " + response + " matched expected response: " + expectedResponse);
                    authed = true;
                    out.println("authenticated");
                    out.flush();
                    break;
                } else {
                    System.err.println("Client response wrong: " + response + ", terminating connection.");
                    socket.close();
                    return;
                }
            }
            System.out.println("User " + user + " fully authenticated.");

			String clientMsg = null;
			while ((clientMsg = in.readLine()) != null) {
				System.out.println("received '" + clientMsg + "' from client:" + subject);
				String[] splitMsg = clientMsg.split(" ", 3);
				System.out.println(splitMsg[0]);

				String persNbr = splitMsg[1];
				if (splitMsg[0].equals("ADD") && user.getPosition().equals(User.DR)) {
					String persNr = clientMsg.split(" ")[1];
					String patName = clientMsg.split(" ")[2];
					String docName = clientMsg.split(" ")[3];
					String docID = clientMsg.split(" ")[4];
					String nurseName = clientMsg.split(" ")[5];
					String nurseID = clientMsg.split(" ")[6];
					String division = clientMsg.split(" ")[7];
					String text = clientMsg.split(" ", 9)[8];
					medRecords.addRecord(persNr, new MedRecord(patName, persNr,
							docName, docID, nurseName, nurseID, division, text));
				}
				else if (splitMsg[0].equals("APPEND") ) {
					MedRecord tempRecord = medRecords.getRecord(persNbr);
					if (tempRecord != null) {
						if(user.getID() == tempRecord.getDocID() && user.getPosition().equals(User.DR) ||
								user.getID() == tempRecord.getNurseID() && user.getPosition().equals(User.NURSE)) {
							String information = clientMsg.split(" ", 3)[2];
							if (medRecords.getRecord(persNbr) != null) {
								medRecords.getRecord(persNbr).append(information);
							}							
						} else {
							out.println("Permission denied!");
						}
					} else {
						out.println("RECORD NOT FOUND");
					}
					
				}
				else if (splitMsg[0].equals("READ")) {
					MedRecord tempRecord = medRecords.getRecord(persNbr);
					if (tempRecord != null) {
						if((user.getDivision().equals(tempRecord.getDivision()) && !user.getPosition().equals(User.PATIENT)) ||
								user.getID() == tempRecord.getDocID() && user.getPosition().equals(User.DR) ||
								user.getID() == tempRecord.getNurseID() && user.getPosition().equals(User.NURSE) ||
								user.getID() == tempRecord.getPatientID() && user.getPosition().equals(User.PATIENT) ||
								user.getPosition().equals(User.GOV)) {
							out.println(tempRecord);					
						} else {
							out.println("Permission denied!");
						}
					} else {
						out.println("RECORD NOT FOUND");
					}
				}
				else if (splitMsg[0].equals("DELETE") && user.getPosition().equals(User.GOV)) {
					if (medRecords.getRecord(persNbr) != null) {
							medRecords.deleteRecord(persNbr);
					} else {
						out.println("RECORD NOT FOUND");
					}					
				} else {
					out.println("Invalid command!");
				}
				out.flush();
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				
				
				log("On " + dateFormat.format(date) + " User: " + user.getID() + " Wrote: " + clientMsg);
				
				System.out.println("done\n");
			}
			in.close();
			out.close();
			socket.close();
			numConnectedClients--;
			System.out.println("client disconnected");
			System.out.println(numConnectedClients
					+ " concurrent connection(s)\n");

		} catch (IOException e) {
			System.out.println("Client died: " + e.getMessage());
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
	
	private void log(String message) throws FileNotFoundException, UnsupportedEncodingException {
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("logfile.txt", true)));
		    out.println(message);
		    out.close();
		} catch (FileNotFoundException ex) {
			PrintWriter writer = new PrintWriter("logfile.txt", "UTF-8");
			writer.println(message);
			writer.close();
		} catch (IOException e) {
		    System.out.println(e.getMessage());
		}
	}

	private void newListener() {
		(new Thread(this)).start();
	} // calls run()

	public static void main(String args[]) {
		medRecords = new RecordDB();
		medRecords = medRecords.loadFromDisk();
		loadUsersFromDisk();
		System.out.println("users loaded:" + users.size() + " " + users);

		System.out.println("\nServer Started\n");
		int port = -1;
		if (args.length >= 1) {
			// Setting up new user (might be a tad unsafe, as it does not check
			// anything
			if (args[0].equals("-add")) {
				try {
					String typeArg = args[1];
					String nameArg = args[2];
					String persNbr = args[3];
                    // Check for unique persNbr
                    if (users.containsKey(persNbr)) {
                        System.err.println("Database already contains a user with specified ID.");
                        System.exit(1);
                    }
					String division = args[4];
					String password = Authenticator.getRandomPassword();
					System.out.println("New password for user " + nameArg
							+ ":\n" + password);
					System.out
							.println("Remember it, this will be the only time it is shown.");
					User newUser = new User(nameArg, typeArg, password, persNbr, division);
					users.put(persNbr, newUser);
					saveUsersToDisk();
					System.exit(0);
				} catch (IllegalArgumentException ex) {
                    System.out.println(ex.getMessage());
					System.err
							.println("Wrong format for option add, the correct format is:\n -add <position {DR, NURSE, PATIENT or GOV}> <name> <ID number> <division>");
					System.exit(1);
				}

            } else {
				port = Integer.parseInt(args[0]);
			}
		}
		String type = "TLS";
		try {
			ServerSocketFactory ssf = getServerSocketFactory(type);
			ServerSocket ss = ssf.createServerSocket(port);
			((SSLServerSocket) ss).setNeedClientAuth(true); // enables client
															// authentication
			new Server(ss);
		} catch (IOException e) {
			System.out.println("Unable to start Server: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Tries to read users file from disk
	// If it is not present, it creates a new HashMap
    @SuppressWarnings("unchecked")
	private static void loadUsersFromDisk() {
		try {
			FileInputStream fileIn = new FileInputStream("users");
			ObjectInputStream in = new ObjectInputStream(fileIn);

			users = (HashMap<String, User>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
            System.err.println("Could not find file \"users\", a new one will be created.");
		} catch (ClassNotFoundException e) { // Probably happens when upgrading
			System.err.println("Could not load file \"users\", delete the file and start over.");
		}
		if (users == null) {
			users = new HashMap<String, User>();
		}

	}

	// Save the users HashMap to disk (when to do this?)
	private static void saveUsersToDisk() {
		try {
			FileOutputStream fileOut = new FileOutputStream("users");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(users);
			out.close();
			fileOut.close();
			System.out.printf("Serialized data is saved in users\n");
		} catch (IOException i) {
			i.printStackTrace();
		}

	}

	private static ServerSocketFactory getServerSocketFactory(String type) {
		if (type.equals("TLS")) {
			SSLServerSocketFactory ssf = null;
			try { // set up key manager to perform server authentication
				SSLContext ctx = SSLContext.getInstance("TLS");
				KeyManagerFactory kmf = KeyManagerFactory
						.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory
						.getInstance("SunX509");
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				char[] password = "password".toCharArray(); // TODO: should
															// probably be read
															// from command line
															// on startup?

				ks.load(new FileInputStream("serverkeystore"), password); // keystore
																			// password
																			// (storepass)
				ts.load(new FileInputStream("servertruststore"), password); // truststore
																			// password
																			// (storepass)
				kmf.init(ks, password); // certificate password (keypass)
				tmf.init(ts); // possible to use keystore as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				ssf = ctx.getServerSocketFactory();
				return ssf;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return ServerSocketFactory.getDefault();
		}
		return null;
	}
}
