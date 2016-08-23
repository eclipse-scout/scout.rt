package org.eclipse.scout.rt.mom.api.encrypter;

import java.security.GeneralSecurityException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.Base64Utility;

/**
 * Provides an initialized AES (Advanced Encryption Standard) cipher (symmetric), and which is configured with values
 * from 'config.properties'.
 * <p>
 * Use this cipher for 'end-to-end' encryption for 'cluster-internal' messaging.
 *
 * @see AesEncrypter
 * @since 6.1
 */
@ApplicationScoped
public class ClusterEncrypter implements IEncrypter {

  protected AesEncrypter m_aesEncrypter;

  @PostConstruct
  public void init() {
    m_aesEncrypter = BEANS.get(AesEncrypter.class)
        .withAlgorithmProvider(CONFIG.getPropertyValue(AlgorithmProviderProperty.class))
        .withPbeSalt(CONFIG.getPropertyValue(PbeSaltProperty.class))
        .withPbePassword(CONFIG.getPropertyValue(PbePasswordProperty.class))
        .withPbeKeyLength(CONFIG.getPropertyValue(PbeKeyLengthProperty.class))
        .withPbeAlgorithm(CONFIG.getPropertyValue(PbeAlgorithmProperty.class))
        .withPbeAlgorithmProvider(CONFIG.getPropertyValue(PbeAlgorithmProviderProperty.class))
        .withPbeIterationCount(CONFIG.getPropertyValue(PbeIterationCountProperty.class))
        .withGcmInitializationVectorByteLength(CONFIG.getPropertyValue(GcmInitializationVectorLengthProperty.class))
        .withGcmAuthTagBitLength(CONFIG.getPropertyValue(GcmAuthTagLengthProperty.class))
        .withPadding(CONFIG.getPropertyValue(PaddingProperty.class))
        .init();
  }

  @Override
  public Map<String, String> newContext() {
    return m_aesEncrypter.newContext();
  }

  @Override
  public byte[] encrypt(final byte[] clearText, final Map<String, String> context) throws GeneralSecurityException {
    return m_aesEncrypter.encrypt(clearText, context);
  }

  @Override
  public byte[] decrypt(final byte[] encryptedData, final Map<String, String> context) throws GeneralSecurityException {
    return m_aesEncrypter.decrypt(encryptedData, context);
  }

  @Override
  public byte[] encryptProperty(final String property, final byte[] clearText, final Map<String, String> context) throws GeneralSecurityException {
    return m_aesEncrypter.encryptProperty(property, clearText, context);
  }

  @Override
  public byte[] decryptProperty(final String property, final byte[] encryptedData, final Map<String, String> context) throws GeneralSecurityException {
    return m_aesEncrypter.decryptProperty(property, encryptedData, context);
  }

  // ==== Configuration properties ==== //

  /**
   * @see AesEncrypter#withAesAlgorithmProvider(String)
   */
  public static class AlgorithmProviderProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.encryption.aes.algorithm_provider";
    }

    @Override
    protected String getDefaultValue() {
      return "SunJCE";
    }
  }

  /**
   * @see AesEncrypter#withPbeSalt(byte[])
   */
  public static class PbeSaltProperty extends AbstractConfigProperty<byte[]> {

    @Override
    public String getKey() {
      return "scout.mom.encryption.aes.pbe.salt";
    }

    @Override
    protected byte[] parse(final String value) {
      return Base64Utility.decode(value);
    }
  }

  /**
   * @see AesEncrypter#withPbePassword(char[])
   */
  public static class PbePasswordProperty extends AbstractConfigProperty<char[]> {

    @Override
    public String getKey() {
      return "scout.mom.encryption.aes.pbe.password";
    }

    @Override
    protected char[] parse(final String value) {
      return value.toCharArray();
    }
  }

  /**
   * @see AesEncrypter#withPbeKeyLength(int)
   */
  public static class PbeKeyLengthProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.encryption.aes.pbe.keylength";
    }

    @Override
    protected Integer getDefaultValue() {
      return 128;
    }
  }

  /**
   * @see AesEncrypter#withPbeAlgorithm(String)
   */
  public static class PbeAlgorithmProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.encryption.aes.pbe.algorithm";
    }

    @Override
    protected String getDefaultValue() {
      return "PBKDF2WithHmacSHA256"; // Password-based key-derivation algorithm ('http://tools.ietf.org/search/rfc2898') using the HmacSHA algorithm ('http://www.ietf.org/rfc/rfc2104.txt') as pseudo-random function.
    }
  }

  /**
   * @see AesEncrypter#withPbeAlgorithmProvider(String)
   */
  public static class PbeAlgorithmProviderProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.encryption.aes.pbe.algorithm_provider";
    }

    @Override
    protected String getDefaultValue() {
      return "SunJCE"; // Sun Java Cryptography Extension Provider
    }
  }

  /**
   * @see AesEncrypter#withPbeIterationCount(int)
   */
  public static class PbeIterationCountProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.encryption.aes.pbe.iteration_count";
    }

    @Override
    protected Integer getDefaultValue() {
      return 3557;
    }
  }

  /**
   * @see AesEncrypter#withGcmInitializationVectorByteLength(int)
   */
  public static class GcmInitializationVectorLengthProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.encryption.aes.gcm.initialization_vector_length";
    }

    @Override
    protected Integer getDefaultValue() {
      return 16;
    }
  }

  /**
   * @see AesEncrypter#withGcmAuthTagBitLength(int)
   */
  public static class GcmAuthTagLengthProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.encryption.aes.gcm.auth_tag_length";
    }

    @Override
    protected Integer getDefaultValue() {
      return 128;
    }
  }

  /**
   * @see AesEncrypter#withPadding(String)
   */
  public static class PaddingProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.encryption.aes.padding";
    }

    @Override
    protected String getDefaultValue() {
      return "PKCS5Padding"; // PKCS5 padding scheme ('http://tools.ietf.org/html/rfc2898'
    }
  }
}
