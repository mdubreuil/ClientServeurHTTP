
package http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import http.Http.ContentType;

/**
 * Class Client : Navigateur Web.
 *
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 */
public class Client extends Thread
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
    private String errorMsg = "";
    
//    public Client(String hostName, int port)
//    {
//        try {
//            
//        } catch (IOException ex) {
//            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
//            if (ex instanceof UnknownHostException) {
//                code = ERR_HOST;
//            }
//            exception = ex;
//        }
//    }
    
    @Override
    public void run()
    {
        int port = -1;
        String hostName, resourcePath, method = Http.METHOD_GET;
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                System.out.print("Hôte (ex: localhost:1030) : ");
                hostName = scanner.nextLine();
                System.out.print("\nChemin relatif de la resource (ex: index.txt) : /");
                resourcePath = scanner.nextLine();
                resourcePath = "/" + resourcePath;
                System.out.println();

                if (hostName.contains(":")) {
                    String[] host = hostName.split(":", 2);
                    port = Integer.valueOf(host[1].trim());
                    
                    socket = new Socket(host[0].trim(), port);
                } else {
                    socket = new Socket(hostName, 80);
                }

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                
                RequestHTTP request = new RequestHTTP(hostName, method, resourcePath);
                
                out.write(request.toString());
                out.write("\r\n");
                // Write content only if POST request, 
                // but our client only send GET request.
                out.flush();
        
                // Read response
                String responseString = "", line;
                while ((line = in.readLine()) != null) {
//                    System.out.println("in");
                    responseString += line + "\r\n";
                    if (line.isEmpty()) {
                        System.out.println("in in");
                        if ((line = in.readLine()) != null) {
                            System.out.println(line);
                            responseString += line;// + "\r\n";
                            if (line.isEmpty()) {
                                responseString += "\r\n";
                                System.out.println("break 1");
                                break;
                            }
                        }
                        System.out.println("break 2");
                        break;
                    }
                }
                System.out.println("sortie whiles");
                
                if (responseString.isEmpty()) {
                    // code error
                    //continue;
                    return;
                }
                System.out.println(responseString);

                ResponseHTTP response = new ResponseHTTP(responseString);
                if (response.getContentType().contains("text")) {
//                    System.out.println(new String(response.getContent()));
                    // setText
                    if (response.getContentType().contains(ContentType.TEXT_HTML.getValue())) {
                        // setText content.getHtml()
                    }
                } else if (response.getContentType().contains("image")) {
                    // setImage
                }
//                System.out.println(requestString);
//                // Création de la requète HTTP
//                RequestHTTP request = new RequestHTTP(requestString);
            }
        } catch (IOException ex) {
            if (ex instanceof SocketException) {
                code = ERR_SOCKET;
            } else if (ex instanceof UnknownHostException) {
                code = ERR_HOST;
            } else {
                code = ERR_PACKET;
            }
            errorMsg = "Return code : " + code + " [ " + ex.getClass() + " - " + ex.getMessage() + " ] ";
            System.err.println(errorMsg);
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    
    public static void main(String[] args)
    {
        //Client client = new Client();
        System.out.println("Launching client...\n");
        (new Client()).start();
    }
}
