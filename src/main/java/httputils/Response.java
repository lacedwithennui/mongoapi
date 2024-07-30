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

    /**
     * The full constructor for a response with a code, body, and headers. 
     * <br></br>
     * Note that the desired structure in this codebase is a fluent interface architecture,
     * so the no-parameter constructor is preferred heavily.
     * For the same reason, there are no other options for constructors with different parameters.
     * @param code an HTTP response code (ex. 200 or 401)
     * @param body the body of the HTTP response, preferably as a String containing JSON.
     * @param headers a hashtable containing key-value pairs of HTTP headers.
     */
    public Response(int code, String body, Hashtable<String, Object> headers) {
        this.code = code;
        this.body = body;
        this.headers = headers;
    }

    /**
     * Sets the response code to the given response code and returns this.
     * @param code the desired response code.
     * @return the Response object with the new response code.
     */
    public Response withCode(int code) {
        this.code = code;
        return this;
    }

    /**
     * Sets the body to the given String and returns this.
     * @param code the desired body (preferrably as a String containing JSON).
     * @return the Response object with the new body.
     */
    public Response withBody(String body) {
        this.body = body;
        return this;
    }

    /**
     * Sets the headers table to the given hashtable and returns this.
     * This hashtable should contain only key-value pairs of HTTP headers.
     * @param code the desired headers table.
     * @return the Response object with the new headers.
     */
    public Response withHeaders(Hashtable<String, Object> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Sets the specified header in the headers table to the given value
     * and returns this.
     * @param key the name of the header to add or modify.
     * @param value the new value of the header.
     * @return the Response object with the new header.
     */
    public Response withHeader(String key, Object value) {
        this.headers.put(key, value);
        return this;
    }

    /**
     * Adds the Access-Control-Allow-Methods header to the headers table with
     * the value "GET", then returns this.
     * @return the Response object with the Access-Control-Allow-Methods header set to "GET".
     */
    public Response withAllowGetMethodHeader() {
        this.headers.put("Access-Control-Allow-Methods", "GET");
        return this;
    }

    /**
     * Adds the Access-Control-Allow-Methods header to the headers table with
     * the value "POST", then returns this.
     * @return the Response object with the Access-Control-Allow-Methods header set to "POST".
     */
    public Response withAllowPostMethodHeader() {
        this.headers.put("Access-Control-Allow-Methods", "POST");
        return this;
    }

    /**
     * Adds the Access-Control-Allow-Methods header to the headers table with
     * the value "*", then returns this.
     * @return the Response object with the Access-Control-Allow-Methods header set to "*".
     */
    public Response withAllowAllMethodsHeader() {
        this.headers.put("Access-Control-Allow-Methods", "*");
        return this;
    }

    /**
     * @param code the new code for this Response to use.
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @param body the new body for this Response to use, preferrably as
     * a String containing JSON.
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * @param headers a hashtable containing only key-value pairs of HTTP headers.
     */
    public void setHeaders(Hashtable<String, Object> headers) {
        this.headers = headers;
    }

    /**
     * Sets the header to the desired value.
     * @param key the name of the HTTP header.
     * @param value the new value of the HTTP header.
     */
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
     * See {@link spark.Response}.
     * @param responseShell A spark response object to modify. This is needed
     *                      because the <code>new spark.Response()</code> 
     *                      constructor is not visible.
     * @return the {@link spark.Response} object with all of the data from this object.
     */
    public spark.Response asSparkResponse(spark.Response responseShell) {
        responseShell.status(this.code);
        headers.forEach((key, value) -> {
            responseShell.header(key, value.toString());
        });
        responseShell.body(body);
        return responseShell;
    }

    /**
     * @param e the exception that caused the 500 error.
     * @return a generic 500 error with the Java exception message as the body.
     */
    public static Response defaultServerError(Exception e) {
        return new Response().withBody("{\"error\": " + e.getMessage() + "}");
    }

    /**
     * @return a generic 401 unauthorized error (user does not have a valid session
     *         or logged in with invalid credentials)
     */
    public static Response unauthorizedError() {
        return new Response().withCode(401).withBody("{\"error\": \"401: You do not have authorization to view or edit this information.\"}");
    }
}
