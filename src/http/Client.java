
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
import java.io.Closeable;
import java.util.Arrays;

/**
 * Class Client : Navigateur Web.
 *
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 */
public class Client implements Closeable
{
    private RequestHTTP request = null;
    private ResponseHTTP response = null;
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
    private boolean changed = false;
    private String hostName = "";
    private String resource = "";
    private String method = Http.METHOD_GET;
    
    public Client(String hostName, String relativeResourcePath)
    {
        try {
            this.hostName = hostName;
            this.resource = relativeResourcePath;
            this.setSocket();
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
        }
    }
    
    private void setSocket() throws IOException
    {
        if (hostName.contains(":")) {
            String[] host = hostName.split(":", 2);
            int port = Integer.valueOf(host[1].trim());

            socket = new Socket(host[0].trim(), port);
        } else {
            socket = new Socket(hostName, 80);
        }
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        changed = false;
    }

//    @Override
//    public void run()
    public void get()
    {
        try {
            if (this.changed) {
                this.setSocket();
            }
            this.request = new RequestHTTP(hostName, method, resource);

            out.write(request.toString());
            out.write("\r\n");
            // Write content only if POST request, 
            // but our client only send GET request.
            out.flush();

            // Read response
            String headerString = "", contentString = "", line;
            boolean isHeader = true;
            //while (true) {
            while (isHeader) {
                line = in.readLine();
                if (line == null) break;
                if (line.length() == 0) {
                    isHeader = false; // end of header
                }
                headerString += line + "\r\n";
                System.out.println(line);
                //} else {
//                    if ((datasize = in.read(data)) == -1) {
//                        System.out.println("break");
//                        break;
//                    }
//                    
//                    line = in.readLine();
//                    if (line == null) {
//                        System.out.println("break 2");
//                        break;
//                    }
//                    if (line.length() == 0) {
//                        System.out.println("break 3");
//                        //break;
//                    }
//                    contentString += line + "\r\n";
                //}
//                System.out.println(line);
            }
            System.out.println(headerString);
            if (!headerString.isEmpty()) {
                this.response = new ResponseHTTP(headerString, contentString);
            }
            
            char[] data = new char[1000000];
            int length = response.getContentLength();
            int datasize = 0, total = 0;
            while (total < length) {
                //System.out.println("content lentgh :" + total);
                if ((datasize = in.read(data)) == -1) {
                    break;
                }
                total += datasize;
            }
            contentString = new String(data, 0, length);
            System.out.println(contentString);
            this.response.setContent(contentString.getBytes());
            out.flush();
        } catch (IOException ex) {
//            if (ex instanceof SocketException) {
//                code = ERR_SOCKET;
//            } else if (ex instanceof UnknownHostException) {
//                code = ERR_HOST;
//            } else {
//                code = ERR_PACKET;
//            }
//            errorMsg = "Return code : " + code + " [ " + ex.getClass() + " - " + ex.getMessage() + " ] ";
//            System.err.println(errorMsg);
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public RequestHTTP getRequest() {
        return request;
    }

    public void setRequest(RequestHTTP request) {
        this.request = request;
    }

    public ResponseHTTP getResponse() {
        return response;
    }

    public void setResponse(ResponseHTTP response) {
        this.response = response;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.changed = !this.request.equals(request) || this.changed;
        this.hostName = hostName;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
        this.in.close();
        this.out.close();
    }
}
