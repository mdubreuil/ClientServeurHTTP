
package http;

import java.util.HashMap;
import java.util.Map;

public class RequestHTTP
{
    private String protocol = "";
    private String method = "";
    private String resource = "";
    private Map<String, String> params = new HashMap();
    private String connection = "";
    private String contentType = "";
    private int contentLength = 0;
    private String content = "";
    
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
            this.resource = resourceArray[0];

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
            // TODO : GETCONTENT (byte[])
            this.content = "TEST";
        }
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

    public String getContent() {
        return content;
    }
}
