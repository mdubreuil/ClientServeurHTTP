
package http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class Client : Navigateur Web.
 *
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 */
public class Client implements Runnable
{
    private Socket socket = null;
    private BufferedReader in = null;
    private BufferedWriter out = null;
    
    // ERROR CODES
    private static final int ERR_SOCKET = -1;
    private static final int ERR_PACKET = -2;
    private static final int ERR_TIMEOUT = -3;
    private static final int ERR_FILEUNKNOWN = -4;
    private static final int ERR_HOST = -5;
    private static final int ERR_STREAM = -6;
    
    private int code = 0;
    private Exception exception = null;
    
    public Client(String hostName, int port)
    {
        try {
            socket = new Socket(hostName, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            if (ex instanceof UnknownHostException) {
                code = ERR_HOST;
            }
            exception = ex;
        }
    }
    
    @Override
    public void run()
    {
        
    }
    
    private void get(String absoluteRemoteFilePath)
    {
        try {
            // Client GET request
            
            String hostName = socket.getInetAddress().getHostName();
            int port = socket.getPort();
            out.write("GET " + absoluteRemoteFilePath + " HTTP/1.1\r\n");
            out.write("Host: " + hostName+":"+port);
            out.write("Accept-Encoding: text/html, image/gif, image/jpeg");
            out.write("Accept-Language: fr");
            out.write("\r\n");
            
            out.write("Date: Fri, 31 Dec 1999 23:59:59 GMT\r\n");
            out.write("Server: Apache/0.8.4\r\n");
            out.write("Content-Type: text/html\r\n");
            out.write("Content-Length: 59\r\n");
            out.write("Expires: Sat, 01 Jan 2000 00:59:59 GMT\r\n");
            out.write("Last-modified: Fri, 09 Aug 1996 14:21:40 GMT\r\n");
            out.write("\r\n");
            out.write("<TITLE>Exemple</TITLE>");
            out.write("<P>Ceci est une page d'exemple.</P>");
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            code = ERR_STREAM;
        }
    }
}
