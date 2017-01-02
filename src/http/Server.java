
package http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import http.Http.ContentType;

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
    
    // ERROR CODES
    private static final int ERR_SOCKET = -1;
    private static final int ERR_PACKET = -2;
    private static final int ERR_TIMEOUT = -3;
    private static final int ERR_FILEUNKNOWN = -4;
    private static final int ERR_HOST = -5;
    private int code = 0;
    private Exception exception = null;

    public Server(Socket socket)
    {
        clientSocket = socket;
    }

    @Override
    public void run()
    {
        System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "\n");

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // default buffer in size : 2048 octet
            out = new DataOutputStream(clientSocket.getOutputStream()); // default buffer out size : 512 octet

            while (true) {
                // Initialization
                String requestString = "", line = null;
            
                // Read request
                while ((line = in.readLine()) != null) {
                    requestString += line + "\r\n";
                    if (line.isEmpty()) {
//                        if ((line = in.readLine()) != null) {
//                            //requestString += "\r\n" + line + "\r\n";
//                        }
                        break;
                    }
                }

                if (requestString.isEmpty()) {
                    //System.err.println("Erreur de réception de la requète client");
                    // Que faire lorsque le client n'a plus envoyé de requète depuis longtemps ?
                    // - Arréter la communication : return;
                    // - Attendre :
                    continue;
                }
                
                System.out.println("-> From client " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                System.out.println(requestString);

                // Création de la requète HTTP
                RequestHTTP request = new RequestHTTP(requestString);

                // Check client and server are using same HTTP protocol version
                if(!request.getProtocol().equals(Http.HTTP1_1)) {
                    System.err.println("Client using different protocol than server : " + request.getProtocol());
                    // Do nothing
                }

                // Handle request
                switch (request.getMethod()) {
                    case Http.METHOD_GET:
                        // Handle GET response
                        ResponseHTTP response = new ResponseHTTP();
                        
                        File resourceFile = new File(request.getResource());
                        
                        if (!resourceFile.exists()) {
                            response.setCode(Http.CODE_NOT_FOUND);
                        } else if (!resourceFile.canRead()) {
                            response.setCode(Http.CODE_FORBIDDEN);
                        } else {
                            // Read resource
                            long dataSize = resourceFile.length();
                            byte[] data = new byte[(int) dataSize + 1];
                            int dataReadSize;

                            FileInputStream fileReader = new FileInputStream(resourceFile);
                            do {
                                dataReadSize = fileReader.read(data);
                            } while (dataReadSize != -1);
                            fileReader.close();

                            String[] resourcePathArray = request.getResource().split("\\.");
                            String extension = resourcePathArray[resourcePathArray.length - 1];
                            
                            response.setContentType(ContentType.getValueByExtension(extension));
                            response.setContent(data);
                        }
                        
                        // Send Server GET response
                        out.writeBytes(response.toString());
                        out.writeBytes("\r\n");
                        out.write(response.getContent());
                        //out.writeBytes("\r\n");
                        out.flush();
                        break;
                    case Http.METHOD_POST:
                        System.out.println("POST request : non pris en charge");
                        break;
                    default:
                        System.out.println("Not a GET or POST request");
                        break;
                }

                if (request.getConnection().equalsIgnoreCase(Http.CONNECTION_CLOSE)) {
                    // Demande de fin de connexion par le client
                    System.err.println("Connexion avec le client terminée");
                    out.close();
                    in.close();
                    clientSocket.close();

                    return;
                }
            }
            
        } catch (IOException ex) {
            if(ex instanceof SocketException) {
                // Connexion interrompue par le client
                System.err.println("Connexion interrompue par le client");
            }
            // Traiter l'interruption de la connexion par le client
            // Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private byte[] readFile(File resourceFile) throws IOException
    {
        // Read resource
        long dataSize = resourceFile.length();
        byte[] data = new byte[(int) dataSize + 1];
        int dataReadSize;

        FileInputStream fileReader = new FileInputStream(resourceFile);
        do {
            dataReadSize = fileReader.read(data);
        } while (dataReadSize != -1);
        
        return data;
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
