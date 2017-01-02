
package http.test;

import http.Client;
import http.Server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class Test : Classe de test d'une connexion entre serveur et client HTTP.
 * 
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 */
public class Test
{
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
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
