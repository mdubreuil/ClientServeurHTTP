
package http;

import http.Http.ContentType;

public class ResponseHTTP
{
    private String code = "";
    private String contentType = "";
    private byte[] content = new byte[]{};

    ResponseHTTP(String response) {
        // Get response as an array
        String[] responseArray = response.split("\r\n");

        // Get first line such as "GET resourceRelativePath HTTP/1.1"
        String[] responseLine = responseArray[0].split(" ", 2);
        this.code = responseLine[1].trim();

        // Check headers
        //for (int i = 1; i <= responseArray.length; i++) 
        int i = 1;
        while (i < responseArray.length - 1 && !responseArray[i].isEmpty()) {
            String[] headerArray = responseArray[i].split(":");
            String headerName = headerArray[0].trim();
            String headerValue = headerArray[1].trim();

            switch (headerName) {
//                case Http.CONNECTION:
//                    this.connection = headerValue;
//                    break;
//                case Http.CONTENT_LENGTH:
//                    this.contentLength = Integer.valueOf(headerValue);
//                    break;
                case Http.CONTENT_TYPE:
                    this.contentType = headerValue;
                    break;
            }
            i++;
        }
        
        // Gestion du content de la request
        this.content = responseArray[responseArray.length - 1].getBytes();
        // Todo set encoding
    }
    
    
    
    public ResponseHTTP()
    {
        this.code = Http.CODE_OK;
        this.contentType = ContentType.TEXT_HTML.getValue();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString()
    {
        String s = "";
        
        s += Http.HTTP1_1 + " " + this.code + "\r\n";
        s += Http.CONTENT_TYPE + ": " + this.contentType + "\r\n";
        s += Http.CONTENT_LENGTH + ": " + this.content.length + "\r\n";

        return s;
    }
}
