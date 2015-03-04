import java.io.*;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.util.HashMap;

import javax.naming.AuthenticationNotSupportedException;
import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

public class Server implements Runnable {
    private ServerSocket serverSocket = null;
    private static int numConnectedClients = 0;
    private static HashMap<String, User> users;
    private static RecordDB medRecords;

    public Server(ServerSocket ss) throws IOException {
        serverSocket = ss;
        newListener();
    }

    public void run() {
        try {
            SSLSocket socket=(SSLSocket)serverSocket.accept();
            newListener();
            SSLSession session = socket.getSession();
            X509Certificate cert = (X509Certificate)session.getPeerCertificateChain()[0];
			String issuer = cert.getIssuerDN().getName();
            String subject = cert.getSubjectDN().getName();
			String serial = cert.getSerialNumber().toString();
			subject = subject.substring(3);  // CN=Name -> Name
            // TODO: Perform authentication, we here know which user is connected via the certs
			User user = Authenticator.authenticateUser(subject, users);
            // TODO: Send Nonce and verify response
            if (user != null) {
            	System.out.println("User: " + user.toString() + " logged in");
                // Now we know that the user is authenticated
            }
            
            
            numConnectedClients++;
            PrintWriter out = null;
            BufferedReader in = null;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	out.println("Authenticated as user: " + user);
        	out.println("do what you want to do");
            String clientMsg = null;
            while ((clientMsg = in.readLine()) != null) {
                System.out.println("received '" + clientMsg + "' from client:" + subject);
                String[] splitMsg = clientMsg.split(" ", 2);
                System.out.println(splitMsg[0]);
                if(splitMsg[0].equals("ADD")) {
                	int persNr = Integer.parseInt(clientMsg.split(" ")[1]);
                	String patName = clientMsg.split(" ")[2];
                	String docName = clientMsg.split(" ")[3];
                	String nurseName = clientMsg.split(" ")[4];
                	String division = clientMsg.split(" ")[5];
                	String text = clientMsg.split(" ", 7)[6];
                	medRecords.addRecord(persNr, new MedRecord(patName, docName, nurseName, division, text));
                }
                if(splitMsg[0].equals("APPEND")) {
                	System.out.println("vill appenda");
                }
                if(splitMsg[0].equals("READ")) {
                	int persNbr = Integer.parseInt(splitMsg[1]);
                	System.out.println("Vill läsa");
                	System.out.println(medRecords.getRecord(persNbr).toString());
                	out.println(medRecords.getRecord(persNbr).toString());
                }
                if(splitMsg[0].equals("DELETE")) {
                	System.out.println("Vill radera");
                }
				out.flush();
                System.out.println("done\n");
			}
			in.close();
			out.close();
			socket.close();
    	    numConnectedClients--;
            System.out.println("client disconnected");
            System.out.println(numConnectedClients + " concurrent connection(s)\n");
            System.out.println(medRecords.getRecord(123456));
            
		} catch (IOException e) {
            System.out.println("Client died: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    private void newListener() { (new Thread(this)).start(); } // calls run()

    public static void main(String args[]) {
    	medRecords = new RecordDB();
    	medRecords = medRecords.loadFromDisk();
        loadUsersFromDisk();
        System.out.println("users loaded:" + users.size() + " " + users);

        System.out.println("\nServer Started\n");
        int port = -1;
        if (args.length >= 1) {
            // Setting up new user (might be a tad unsafe, as it does not check anything
            if (args[0].equals("-add")) {
                try {
                    User.Position position = User.Position.valueOf(args[1]);
                    String nameArg = args[2];
                    System.out.println("Setting up new user: " + position + " " + nameArg);

                    String password = Authenticator.getRandomPassword();
                    System.out.println("New password for user " + nameArg + ": " + password);
                    System.out.println("Remember it, this will be the only time it is shown.");
                    User newUser = new User(nameArg, position, password);
                    System.out.println("newUSeR: " + newUser);
                    users.put(nameArg, newUser);
                    saveUsersToDisk();
                    System.exit(0);
                } catch (IllegalArgumentException ex) {
                    System.err.println("Wrong Format for field \"position\", the correct formats are: DR, NURSE, PATIENT or GOV");
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
            ((SSLServerSocket)ss).setNeedClientAuth(true); // enables client authentication
            new Server(ss);
        } catch (IOException e) {
            System.out.println("Unable to start Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

	// Tries to read users file from disk
    // If it is not present, it creates a new HashMap
    private static void loadUsersFromDisk() {
        try
        {
            FileInputStream fileIn = new FileInputStream("users");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            users = (HashMap<String, User>) in.readObject();
            in.close();
            fileIn.close();
        } catch(IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } 
        if (users == null) {
            users = new HashMap<String, User>();
        }
        

    }

    // Save the users HashMap to disk (when to do this?)
    private static void saveUsersToDisk() {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream("users");
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
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
                char[] password = "password".toCharArray(); // TODO: should probably be read from command line on startup?

                ks.load(new FileInputStream("serverkeystore"), password);  // keystore password (storepass)
                ts.load(new FileInputStream("servertruststore"), password); // truststore password (storepass)
                kmf.init(ks, password); // certificate password (keypass)
                tmf.init(ts);  // possible to use keystore as truststore here
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
