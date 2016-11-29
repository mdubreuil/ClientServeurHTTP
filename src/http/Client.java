
package http;

import java.io.IOException;
import java.net.Socket;
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
    
    // ERROR CODES
    private static final int ERR_SOCKET = -1;
    private static final int ERR_PACKET = -2;
    private static final int ERR_TIMEOUT = -3;
    private static final int ERR_FILEUNKNOWN = -4;
    private static final int ERR_HOST = -5;
    private int code = 0;
    private Exception exception = null;
    
    public Client(String hostName, int port)
    {
        try {
            socket = new Socket(hostName, port);
        } catch (IOException ex) {
            code = 1;
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            exception = ex;
        }
    }
    
    @Override
    public void run() {
        
    }
    
}
