
package http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class Server : A concurrent HTTP 1.1 server.
 * 
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 */
public class Server implements Runnable
{
    private ServerSocket serverSocket = null;
    
    // ERROR CODES
    private static final int ERR_SOCKET = -1;
    private static final int ERR_PACKET = -2;
    private static final int ERR_TIMEOUT = -3;
    private static final int ERR_FILEUNKNOWN = -4;
    private static final int ERR_HOST = -5;
    private int code = 0;
    private Exception exception = null;
    
    public Server(int port, int queueLength)
    {
        try {
            serverSocket = new ServerSocket(port, queueLength);
        } catch (IOException ex) {
            if(ex instanceof BindException) {
                code = ERR_SOCKET;
            }
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            exception = ex;
        }
    }

    @Override
    public void run() {
        BufferedReader in = null;
        BufferedWriter out = null;
        Socket clientSocket = null;
        
        while (true) { // repeatedly wait for connections, and process
            try {
                clientSocket = serverSocket.accept();
                System.err.println("Nouveau client connecté");
                
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // default buffer in size : 2048 octet
                out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())); // default buffer out size : 512 octet
                
                // chaque fois qu'une donnée est lue sur le réseau on la renvoi sur
                // le flux d'écriture.
                // la donnée lue est donc retournée exactement au même client.
                String s;
                while ((s = in.readLine()) != null) {
                    System.out.println(s);
                    if (s.isEmpty()) {
                        break;
                    }
                }

                out.write("HTTP/1.0 200 OK\r\n");
                out.write("Date: Fri, 31 Dec 1999 23:59:59 GMT\r\n");
                out.write("Server: Apache/0.8.4\r\n");
                out.write("Content-Type: text/html\r\n");
                out.write("Content-Length: 59\r\n");
                out.write("Expires: Sat, 01 Jan 2000 00:59:59 GMT\r\n");
                out.write("Last-modified: Fri, 09 Aug 1996 14:21:40 GMT\r\n");
                out.write("\r\n");
                out.write("<TITLE>Exemple</TITLE>");
                out.write("<P>Ceci est une page d'exemple.</P>");
        
                // on ferme les flux.
                System.err.println("Connexion avec le client terminée");
                out.close();
                in.close();
                clientSocket.close();

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
            }
        }
    }
}
