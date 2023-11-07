import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class Crypto {
    private MessageDigest sha;
    private Cipher cipher;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * A simple handful of encryption and hashing methods.
     */
    public Crypto() {
        try {
            this.sha = MessageDigest.getInstance("SHA3-256");
            this.cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(Credentials.privKey.getBytes()));
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(Credentials.pubKey.getBytes()));
            KeyFactory factory = KeyFactory.getInstance("RSA");
            this.privateKey = factory.generatePrivate(privateSpec);
            this.publicKey = factory.generatePublic(publicSpec);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }

    /**
     * Decrypts a given value using the private key
     * @param encryptedValue the encrypted value to decrypt
     * @return the decrypted value
     */
    public String decrypt(String encryptedValue) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            String decrypted = new String(cipher.doFinal(Base64.getDecoder().decode(encryptedValue.getBytes())));
            return decrypted;
        }
        catch(Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    /**
     * Encrypts a given value using the public key
     * @param unencryptedValue the value to encrypt
     * @return the encrypted value as a base64 string
     */
    public String encrypt(String unencryptedValue) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(unencryptedValue.getBytes())); 
        }
        catch(Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    /**
     * Creates a SHA3-256 hash of the unhashed password + the username + the random salt contained in {@link Credentials}.
     * @param unhashed The plaintext password to hash
     * @param username The username to include in the hash
     * @return the SHA3-256 hash as a hex string
     */
    public String saltedHash(String unhashed, String username) {
        return new BigInteger(sha.digest((unhashed + username + Credentials.salt).getBytes())).toString(16);
    }
}