import java.util.Hashtable;

public class HTTPHelper {
    public static class Response {
        private int code;
        private String data;
        private Hashtable<String, Object> headers;
    
        public Response() {
            this(500, "", new Hashtable<>());
        }
    
        public Response(int code, String data, Hashtable<String, Object> headers) {
            this.code = code;
            this.data = data;
            this.headers = headers;
        }
    
        public Response withCode(int code) {
            this.code = code;
            return this;
        }
    
        public Response withData(String data) {
            this.data = data;
            return this;
        }
    
        public Response withHeaders(Hashtable<String, Object> headers) {
            this.headers = headers;
            return this;
        }
    
        public Response withHeader(String key, Object value) {
            this.headers.put(key, value);
            return this;
        }
    
        public void setCode(int code) {
            this.code = code;
        }
    
        public void setData(String data) {
            this.data = data;
        }
    
        public void setHeaders(Hashtable<String, Object> headers) {
            this.headers = headers;
        }
    
        public void setHeader(String key, Object value) {
            this.headers.put(key, value);
        }
    
        public int getCode() {
            return this.code;
        }
    
        public String getData() {
            return this.data;
        }
    
        public Hashtable<String, Object> getHeaders() {
            return this.headers;
        }
    
        public Object getHeader(String key) {
            return this.headers.get(key);
        }
    }
}
