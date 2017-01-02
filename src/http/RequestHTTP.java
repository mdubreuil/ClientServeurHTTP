
package http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHTTP
{
    private String protocol = "";
    private String method = "";
    private String resource = "";
    private Map<String, String> params = new HashMap();
    private String connection = "";
    private String contentType = "";
    private int contentLength = 0;
    private byte[] content = new byte[]{};
    private String host = "";
    
    public RequestHTTP(String request)
    {
        // Get request as an array
        String[] requestArray = request.split("\r\n");

        // Get first line such as "GET resourceRelativePath HTTP/1.1"
        String[] requestLine = requestArray[0].split(" ");
        this.method = requestLine[0].trim();
        this.protocol = requestLine[2];

        // Gestion de la ressource et des paramètres
        String resource = requestLine[1].trim();
        this.resource = (resource.charAt(0) == '/') ? resource.substring(1) : resource;
        if (this.resource.contains("?"))
        {
            String[] resourceArray = this.resource.split("\\?");
            try {
                this.resource = URLDecoder.decode(resourceArray[0], "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                System.out.println(this.resource);
                Logger.getLogger(RequestHTTP.class.getName()).log(Level.SEVERE, null, ex);
            }

            String[] paramsArray = resourceArray[1].split("&");
            for (String param : paramsArray) {
                String[] paramArray = param.split("=");
                String paramName = paramArray[0].trim();
                String paramValue = paramArray[1].trim();
                this.params.put(paramName, paramValue);
            }
        }

        // Check headers
        for (int i = 1; i < requestArray.length; i++) 
        {
            String[] headerArray = requestArray[i].split(":");
            String headerName = headerArray[0].trim();
            String headerValue = headerArray[1].trim();

            switch (headerName) {
                case Http.CONNECTION:
                    this.connection = headerValue;
                    break;
                case Http.CONTENT_LENGTH:
                    this.contentLength = Integer.valueOf(headerValue);
                    break;
                case Http.CONTENT_TYPE:
                    this.contentType = headerValue;
                    break;
//                default:
//                    System.err.println("Unknown header : " + requestArray[i]);
//                    break;
            }
        }
        
        // Gestion du content de la request
        if (this.contentLength > 0)
        {
            // Todo set encoding
            this.content = requestArray[requestArray.length - 1].getBytes();
        }
    }

    RequestHTTP(String host, String method, String resource) {
        this.host = host;
        this.method = method;
        this.resource = resource.startsWith("/") ? resource.substring(1) : resource;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getMethod() {
        return method;
    }

    public String getResource() {
        return resource;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getConnection() {
        return connection;
    }

    public String getContentType() {
        return contentType;
    }

    public int getContentLength() {
        return contentLength;
    }
    
    public byte[] getContent() {
        return this.content;
    }
    
    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
    
    @Override
    public String toString()
    {
        String s = "";
        
        s += this.method + " " + this.resource + " " + Http.HTTP1_1 + "\r\n";
        s += Http.HOST + ": " + this.host + "\r\n";

        return s;
    }
}
