import java.io.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.security.KeyStore;

/*
 * This example shows how to set up a key manager to perform client
 * authentication.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */
public class Client {

    public static void main(String[] args) throws Exception {
        // Initialize variables
        String host = null;
        int port = -1;
        char[] password = null;

        // Check that we have the correct number of args
        if (args.length != 2) {
            System.err.println("USAGE: java client host port");
            System.exit(-1);
        }
        
        // Get the arguments (server host and port number)
        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
            System.out.println("USAGE: java client host port password");
            System.exit(-1);
        }
        
        // Ask for password to key- and truststore
        password = Utils.readPassword("Password to key- and truststores please.");


        try { /* set up a key manager for client authentication */
            SSLSocketFactory factory = null;
            try {
                // Load key- and truststore
                KeyStore ks = KeyStore.getInstance("JKS");
                KeyStore ts = KeyStore.getInstance("JKS");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                
                // Get SSLContext and read key- and truststores
                SSLContext ctx = SSLContext.getInstance("TLS");
                ks.load(new FileInputStream("clientkeystore"), password);
				ts.load(new FileInputStream("clienttruststore"), password);
				kmf.init(ks, password);
				tmf.init(ts);
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                factory = ctx.getSocketFactory();
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
            
            // Create socket
            SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
            System.out.println("\nsocket before handshake:\n" + socket + "\n");

            /*
             * send http request
             *
             * See SSLSocketClient.java for more information about why
             * there is a forced handshake here when using PrintWriters.
             */
            socket.startHandshake();

            SSLSession session = socket.getSession();
            X509Certificate cert = (X509Certificate)session.getPeerCertificateChain()[0];
            String subject = cert.getSubjectDN().getName();
			String issuer = cert.getIssuerDN().getName();
			String serial = cert.getSerialNumber().toString();
            System.out.println("certificate name (subject DN field) on certificate received from server:\n" + subject + "\n");
			System.out.println("certificate issuer on cert recieved from server:\n" + issuer + "\n");
			System.out.println("certificate serial number on cert recieved from server:\n" + serial + "\n");
            System.out.println("socket after handshake:\n" + socket + "\n");
            System.out.println("secure connection established\n\n");

            BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
			for (;;) {
                System.out.print(">");
                msg = read.readLine();
                if (msg.equalsIgnoreCase("quit")) {
				    break;
				}
                System.out.print("sending '" + msg + "' to server...");
                out.println(msg);
                out.flush();
                System.out.println("done");
                String nextLine;
                while((nextLine = in.readLine()) != null) {
                	System.out.println(/*"received '" + */nextLine/* + "' from server\n"*/);
                    
                    if (nextLine.startsWith("challenge: ")) {
                        System.out.println("Server sent a challenge: " + nextLine);
                        String response = ResponseGenerator.getResponse(nextLine);
                        System.out.println("Response is: " + response);
                        // TODO actually send response to server
                    }
                    
                }
            }
            in.close();
			out.close();
			read.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
