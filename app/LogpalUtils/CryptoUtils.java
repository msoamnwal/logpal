package LogpalUtils;

import org.jasypt.util.text.StrongTextEncryptor;
import org.jasypt.util.text.TextEncryptor;

import play.Logger;
import play.Play;

/**
 * Utilities for encryption and decryption.
 */
public class CryptoUtils {

    /**
     * Name of environment variable used to store the key for encryption/decryption.
     * This must match the system property of the same name specified in appengine-web.xml.
     */
    private static final String CRYPTO_KEY_ENV_VARIABLE = "gAudit-cryptoKey";

    private final TextEncryptor encryptor = newEncryptor();

    public String encrypt(String text) {
    	if(text!=null&& !"".equalsIgnoreCase(text)){
    		return encryptor.encrypt(text);
    	}
    	return "";
    }

    public String decrypt(String text) {
    	if(text!=null&& !"".equalsIgnoreCase(text)){
    		return encryptor.decrypt(text);
    	}
    	return "";
        
    }

    private static TextEncryptor newEncryptor() {
        String cryptoKey = Play.configuration.getProperty(CRYPTO_KEY_ENV_VARIABLE);
        //Logger.info("*****CRYPTO_KEY_ENV_VARIABLE ::"+Play.configuration.getProperty(CRYPTO_KEY_ENV_VARIABLE));
        if (cryptoKey == null && !"".equalsIgnoreCase(cryptoKey))
            throw new IllegalArgumentException(
                "Please specify the '" + CRYPTO_KEY_ENV_VARIABLE + "' system property in appengine-web.xml and redeploy.");

        StrongTextEncryptor encryptor = new StrongTextEncryptor();
        // This is a strong encryptor which uses algorithm PBE with MD5 and Triple DES and 1,000 key obtention iterations,
        // as documented at: http://www.jasypt.org/api/jasypt/1.8/org/jasypt/util/text/StrongTextEncryptor.html
        encryptor.setPassword(cryptoKey);
        return encryptor;
    }
}
