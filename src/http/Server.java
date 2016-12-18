
package http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class Server : A concurrent HTTP 1.1 server.
 * 
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 */
public class Server extends Thread
{
    private Socket clientSocket = null;
    private BufferedReader in = null;
    private DataOutputStream out = null;
    // private BufferedWriter out = null;
    
    // ERROR CODES
    private static final int ERR_SOCKET = -1;
    private static final int ERR_PACKET = -2;
    private static final int ERR_TIMEOUT = -3;
    private static final int ERR_FILEUNKNOWN = -4;
    private static final int ERR_HOST = -5;
    private int code = 0;
    private Exception exception = null;
    
    private static final String HTTP1_1 = "HTTP/1.1";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String CODE_OK = "200 OK";
    private static final String CODE_NOT_FOUND = "404 Not Found";

    public Server(Socket socket) // make private => Server.getInstance()
    {
        clientSocket = socket;
    }

    @Override
    public void run()
    {
        System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "\n");

        try {
            // Initialization
            boolean close = false;
            String request = "", line = null;
            
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // default buffer in size : 2048 octet
            out = new DataOutputStream(clientSocket.getOutputStream());
            //out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())); // default buffer out size : 512 octet

            while (in.ready()) {
                // Read request
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                    request = request.concat(line+"\r\n");
                    if (line.isEmpty()) {
                        break;
                    }
                }
                if (request.isEmpty()) {
                    System.err.println("Erreur de réception de la requète client");
                    return;
                }

                // Get request as an array
                String[] requestArray = request.split("\r\n");

                // Get first line of type "GET resourceRelativePath HTTP/1.1"
                String[] requestLine = requestArray[0].split(" ");
                String method = requestLine[0].trim();
                String resource = requestLine[1].trim();
                resource = (resource.charAt(0) == '/') ? resource.substring(1) : resource;

                // Check client and server are using same HTTP protocol version
                String protocol = requestLine[2];
                if(!protocol.equals(HTTP1_1)) {
                    // Do nothing
                    System.err.println("Client using different protocol than server : " + protocol);
                }

                // Handle request
                if (method.equals(METHOD_GET)) {
                    // Check headers : inutile
                    for (int i = 1; i < requestArray.length; i++) {
                        String[] headerArray = requestArray[i].split(":");
                        String headerName = headerArray[0].trim();
                        String headerValue = headerArray[1];

                        //System.out.println(requestArray[i]);
                        switch (headerName) {
                            case "Connection":
                                close = headerValue.equalsIgnoreCase("close");
    //                        case "Content-Length":
    //                            int contentLength = Integer.valueOf(headerArray[1]);
    //                            break;
    //                        case "Accept-Encoding":
    //                            String charset = headerArray[1];
    //                            break;
    //                        case "Accept-Language":
    //                            String language = headerArray[1];
    //                            break;
    //                        default:
    //                            System.err.println("Unknown header : " + requestArray[i]);
    //                            break;
                        }
                    }

                    // Handle GET response
                    String codeResponse = CODE_OK;
                    String contentType = "text/html";
                    byte[] data = null;
                    long dataSize = 0;
                    int dataReadSize;

                    try {
                        // Get resource size for the buffer size
                        File resourceFile = new File(resource);
                        dataSize = resourceFile.length();
                        data = new byte[(int)dataSize+1];

                        FileInputStream fileReader = new FileInputStream(resourceFile);
                        do {
                            dataReadSize = fileReader.read(data);
                        } while(dataReadSize != -1);
                        fileReader.close();

                        String[] resourcePathArray = resource.split("\\.");
                        String extension = resourcePathArray[resourcePathArray.length - 1];
                        if(extension.equalsIgnoreCase("html")) {
                            contentType = "text/html";
                        } else if(extension.equalsIgnoreCase("txt")) {
                            contentType = "text/plain";
                        } else {
                            contentType = "";
                        }
    //                    if (extension.equalsIgnoreCase("jpg") && extension.equalsIgnoreCase("jpeg")) {
    //                        contentType = "image/jpg";
    //                    } else if(extension.equalsIgnoreCase("png")) {
    //                        contentType = "image/png";
    //                    }
                    } catch(IOException ex) {
                        System.err.println("404: " + ex.getMessage());
                        codeResponse = CODE_NOT_FOUND;
                    }

                    // Send Server GET response
                    out.writeBytes(HTTP1_1 + " " + codeResponse + "\r\n");
                    out.writeBytes("Content-Type: " + contentType + "\r\n");
                    out.writeBytes("Content-Length: " + dataSize + "\r\n");
                    out.writeBytes("\r\n");
                    out.write(data);
                    out.flush();
                } else if (method.equals(METHOD_POST)) {
                    System.out.println("POST request");
                } else {
                    System.out.println("Not a GET or POST request");
                }

                if (close) {
                    // on ferme les flux.
                    System.err.println("Connexion avec le client terminée");
                    out.close();
                    in.close();
                    clientSocket.close();
                }
            }
            
        } catch (IOException ex) {
            if(ex instanceof SocketException) {
                System.err.println("Connexion avec le client terminée");
//                    out.close();
//                    in.close();
//                    clientSocket.close();
            }
            // Traiter l'interruption de la connexion par le client
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
    }
    
    public static void main(String[] args)
    {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(1030);
            System.out.println("Server is runnig...\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                (new Server(clientSocket)).start();
            }

        } catch (IOException ex) {
//            if(ex instanceof BindException) {
//                code = ERR_SOCKET;
//            }
//            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
//            exception = ex;
            //serverSocket.close();
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

/*
Démarche :

1) Faire le serveur
2) Tester le serveur avec un navigateur web ou à Telnet => à activer sur Microsoft dans les accessoires (création de req^ètes TCP)
3) Vendredi faire le client Web
4) Tester le client avec note server

FIN : Avoir bien avancé pour vendredi. + 2h à la rentrée en janvier.
*/
