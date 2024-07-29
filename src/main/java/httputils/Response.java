package httputils;
import java.util.Hashtable;

public class Response {
    private int code;
    private String body;
    private Hashtable<String, Object> headers;

    /**
     * Creates a default response with an empty body, no headers, and a code 500.
     * The default code being 500 is intended to not only make error handling easier,
     * but also to ensure that HTTP codes are being set and used with intention rather
     * than ignoring or being lazy with them.
     */
    public Response() {
        this(500, "", new Hashtable<>());
        this.headers.put("Access-Control-Allow-Origin", "*");
    }

    public Response(int code, String body, Hashtable<String, Object> headers) {
        this.code = code;
        this.body = body;
        this.headers = headers;
    }

    public Response withCode(int code) {
        this.code = code;
        return this;
    }

    public Response withBody(String body) {
        this.body = body;
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

    public Response withAllowGetMethodHeader() {
        this.headers.put("Access-Control-Allow-Methods", "GET");
        return this;
    }

    public Response withAllowPostMethodHeader() {
        this.headers.put("Access-Control-Allow-Methods", "POST");
        return this;
    }

    public Response withAllowAllMethodsHeader() {
        this.headers.put("Access-Control-Allow-Methods", "*");
        return this;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setBody(String body) {
        this.body = body;
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

    public String getBody() {
        return this.body;
    }

    public Hashtable<String, Object> getHeaders() {
        return this.headers;
    }

    public Object getHeader(String key) {
        return this.headers.get(key);
    }

    /**
     * Converts this object into a spark response object.
     * @param responseShell The initial spark response object to modify. This
     *                      is needed as the <code>new Response()</code> constructor is not
     *                      visible.
     * @return
     */
    public spark.Response asSparkResponse(spark.Response responseShell) {
        responseShell.status(this.code);
        headers.forEach((key, value) -> {
            responseShell.header(key, value.toString());
        });
        responseShell.body(body);
        return responseShell;
    }

    public static Response defaultServerError(Exception e) {
        return new Response().withBody("{\"error\": " + e.getMessage() + "}");
    }

    public static Response unauthorizedError() {
        return new Response().withCode(401).withBody("{\"error\": \"401: You do not have authorization to view or edit this information.\"}");
    }
}
