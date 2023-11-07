import com.mongodb.ConnectionString;

/** 
 * This file contains credentials and constants that will be used throughout the project. 
 * Please change the name of the file to Credentials for actual use.
 */
public class SampleCredentials {
    /** The beginning of your connection string... up to :// */
    public static final String connectionStringPrefix = "mongodb+srv://";
    /** 
     * The end of your connection string... including and after @. 
     * Please change <EXAMPLE> here to the namespace of your cluster, or replace this string entirely 
     * with the actual connection string suffix to ensure everything goes smoothly.
     */
    public static final String connectionStringSuffix = "@cluster0.<EXAMPLE>.mongodb.net/?retryWrites=true&w=majority";
    /** The username associated with the cluster */
    public static final String username = "<yourmongodbusername>";
    /** The password associated with the cluster, URL Encoded. @ = %40 */
    public static final String password = "<yoururlencodedmongodbpassword>";
    public static final String connectionStringAsString = connectionStringPrefix + username + ":" + password + connectionStringSuffix;
    public static final ConnectionString connectionString = new ConnectionString(connectionStringAsString);
    /** Your PKCS8 RSA Private Key in base64 WITHOUT -----BEGIN PRIVATE KEY-----, newlines, etc */
    public static final String privKey = "MIIBvTBXBgkqhkiG9w0BBQ0wSjApBgkqhkiG9w0BBQwwHAQIpZHwLtkYRb4CAggAMAwGCCqGSIb3DQIJBQAwHQYJYIZIAWUDBAECBBCCGsoP7F4bd8O5I1poTn8PBIIBYBtM1tgqsAQgbSZT0475aHufzFuJuPWOYqiHag8OUKMeZuxVHndElipEY2V5lS9mwddwtWaGuYD/Swcdt0Xht8U8BF0SjSyzQ4YtRsG9CmEHYhWmQ5AqK1W3mDUApO38Cm5L1HrHV4YJnYmmK9jgq+iWlLFDmB8s4TA6kMPWbCENlpr1kEXz4hLwY3ylH8XWI65WX2jGSn61jayCwpf1HPFBPDUaS5s3f92aKjk0AE8htsDBBiCVS3Yjq4QSbhfzuNIZ1TooXT9Xn+EJC0yjVnlTHZMfqrcA3OmVSi4kftugjAax4Z2qDqO+onkgeJAwP75scMcwH0SQUdrNrejgfIzJFWzcH9xWwKhOT9s9hLx2OfPlMtDDSJVRspqwwQrFQwinX0cR9Hx84rSMrFndxZi52o9EOLJ7cithncoW1KOAf7lIJIUzP0oIKkskAndQo2UiZsxgoMYuq02T07DOknc=";
    /** Your X509 RSA Public Key in base64 WITHOUT -----BEGIN PUBLIC KEY-----, newlines, etc */
    public static final String pubKey = "MEgCQQCo9+BpMRYQ/dL3DS2CyJxRF+j6ctbT3/Qp84+KeFhnii7NT7fELilKUSnxS30WAvQCCo2yU1orfgqr41mM70MBAgMBAAE=";
    /** A randomized string that will be factored into all hashes */
    public static final String salt = "AsHjhfkJaWBn#wnIsdqwqoI192KJBkjb13!@j3";
}
