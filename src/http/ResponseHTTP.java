
package http;

public class ResponseHTTP
{
    private String code = "";
    private String contentType = "";
    private byte[] content = new byte[]{};
    
    public enum ContentType
    {	
        TEXT_PLAIN(new String[]{"txt"}, "text/plain"),
        TEXT_HTML(new String[]{"html"}, "text/html"),
        TEXT_CSS(new String[]{"css"}, "text/css"),
        VIDEO_MP4(new String[]{"mp4, mpeg4"}, "video/mp4"),
        IMAGE_JPG(new String[]{"jpg", "jpeg"}, "image/jpg"),
        IMAGE_PNG(new String[]{"png"}, "image/png"),
        ZIP(new String[]{"zip"}, "application/zip");

        private final String[] extension;
        private final String value;

        private ContentType(String[] extension, String value)
        {
            this.extension = extension;
            this.value = value;
        }

        public static String getValueByExtension(String extension)
        {
            String contentType = "charset=utf-8;";

            if (extension.isEmpty()) return TEXT_PLAIN.value + ";" + contentType;
//            if (extension == null || extension.equals("") || extension.equals("."))
//                    return "text/html";

            if (extension.getBytes()[0] == '.')
                extension = extension.substring(1);

            for (ContentType ct : ContentType.values())
                for (String ext : ct.extension)
                    if (ext.equalsIgnoreCase(extension))
                        return ct.value + ";" + contentType;

            return TEXT_HTML.value + ";" + contentType;
        }
    }
    
    public ResponseHTTP()
    {
        this.code = Http.CODE_OK;
        this.contentType = ContentType.TEXT_HTML.value;
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
