package software.wings.generator;

import com.google.inject.Singleton;

import io.harness.exception.WingsException;
import lombok.Getter;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@Singleton
public class ScmSecret {
  protected static final String passphrase = System.getenv("HARNESS_GENERATION_PASSPHRASE");

  @Getter private final Properties secrets;

  ScmSecret() {
    secrets = new Properties();
    try (InputStream in = getClass().getResourceAsStream("/secrets.properties")) {
      secrets.load(in);
    } catch (IOException exception) {
      throw new WingsException(exception);
    }
  }

  public boolean isInitialized() {
    return passphrase != null;
  }

  public byte[] decrypt(SecretName name) {
    return decrypt(secrets.getProperty(name.getValue()));
  }

  public byte[] decrypt(String cipheredSecretHex) {
    if (!isInitialized()) {
      return "You can't decrypt in this environment. You need to set HARNESS_GENERATION_PASSPHRASE in your environment!"
          .getBytes();
    }

    try {
      Key aesKey = new SecretKeySpec(Hex.decodeHex(passphrase.toCharArray()), "AES");
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, aesKey);
      return cipher.doFinal(Hex.decodeHex(cipheredSecretHex.toCharArray()));
    } catch (Exception e) {
      throw new WingsException(e);
    }
  }

  public String decryptToString(SecretName name) {
    return decryptToString(secrets.getProperty(name.getValue()));
  }

  public String decryptToString(String cipheredSecretHex) {
    try {
      return new String(decrypt(cipheredSecretHex), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new WingsException(e);
    }
  }

  public char[] decryptToCharArray(SecretName name) {
    return decryptToCharArray(secrets.getProperty(name.getValue()));
  }

  public char[] decryptToCharArray(String cipheredSecretHex) {
    return decryptToString(cipheredSecretHex).toCharArray();
  }

  public static String encrypt(byte[] secret, String passphrase) {
    try {
      Key aesKey = new SecretKeySpec(Hex.decodeHex(passphrase.toCharArray()), "AES");
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, aesKey);
      return Hex.encodeHexString(cipher.doFinal(secret));
    } catch (Exception e) {
      throw new WingsException(e);
    }
  }

  String encrypt(byte[] secret) {
    return encrypt(secret, passphrase);
  }
}
