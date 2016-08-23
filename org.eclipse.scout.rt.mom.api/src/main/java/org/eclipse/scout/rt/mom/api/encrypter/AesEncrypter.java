package org.eclipse.scout.rt.mom.api.encrypter;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Base64Utility;

/**
 * Provides an 'instance-scoped' AES (Advanced Encryption Standard) cipher for message encryption.
 * <p>
 * Initialize this bean before usage, or use {@link ClusterEncrypter} with default values from <i>config.properties</i>.
 *
 * @since 6.1
 */
@Bean
public class AesEncrypter implements IEncrypter {

  protected static final String PROP_GCM_IV = "gcm.iv";

  protected String m_algorithm;
  protected String m_algorithmProvider;
  protected SecretKey m_secretKey;

  // Configuration used for password-based encryption (PBE) to derive the cryptographic shared key.
  protected byte[] m_pbeSalt;
  protected char[] m_pbePassword;
  protected int m_pbeKeyLength;
  protected String m_pbeAlgorithm;
  protected String m_pbeAlgorithmProvider;
  protected int m_pbeIterationCount;

  // Configuration used for Galois/Counter Mode (GCM) to provide both data authenticity (integrity) and confidentiality.
  protected int m_gcmInitializationVectorByteLength;
  protected int m_gcmAuthTagBitLength;
  protected String m_padding;

  protected AtomicBoolean m_initialized = new AtomicBoolean();
  protected SecureRandom m_random;

  public AesEncrypter init() {
    if (!m_initialized.compareAndSet(false, true)) {
      Assertions.fail("{} already initialized", AesEncrypter.class);
    }
    assertConfiguration();

    m_random = new SecureRandom();

    try {
      // Derive a cryptographic key from given password and salt.
      // PBE = Password-based Encryption
      final SecretKeyFactory factory = SecretKeyFactory.getInstance(m_pbeAlgorithm, m_pbeAlgorithmProvider);
      final KeySpec pbeKeySpec = new PBEKeySpec(m_pbePassword, m_pbeSalt, m_pbeIterationCount, m_pbeKeyLength);
      final byte[] derivedKey = factory.generateSecret(pbeKeySpec).getEncoded();

      m_secretKey = new SecretKeySpec(derivedKey, "AES");
      m_algorithm = String.format("AES/GCM/%s", m_padding);

      // Clear the password
      for (int i = 0; i < m_pbePassword.length; i++) {
        m_pbePassword[i] = '*';
      }
      return this;
    }
    catch (final GeneralSecurityException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public Map<String, String> newContext() {
    final Map<String, String> context = new HashMap<>();

    // Security depends on a unique initialization vector (IV) for every encryption performed.
    // The IV does not need to be secret. Decryption requires both the key and IV.
    final byte iv[] = new byte[m_gcmInitializationVectorByteLength];
    m_random.nextBytes(iv); // generate random IV

    context.put(PROP_GCM_IV, Base64Utility.encode(iv));
    return context;
  }

  @Override
  public byte[] encrypt(final byte[] clearText, final Map<String, String> context) throws GeneralSecurityException {
    assertInitialized();

    final byte[] iv = Base64Utility.decode(context.get(PROP_GCM_IV));
    final GCMParameterSpec gcmSpec = new GCMParameterSpec(m_gcmAuthTagBitLength, iv);

    final Cipher cipher = Cipher.getInstance(m_algorithm, m_algorithmProvider);
    cipher.init(Cipher.ENCRYPT_MODE, m_secretKey, gcmSpec);
    return cipher.doFinal(clearText);
  }

  @Override
  public byte[] decrypt(final byte[] encryptedData, final Map<String, String> context) throws GeneralSecurityException {
    assertInitialized();

    final byte[] iv = Base64Utility.decode(context.get(PROP_GCM_IV));
    final GCMParameterSpec gcmSpec = new GCMParameterSpec(m_gcmAuthTagBitLength, iv);

    final Cipher cipher = Cipher.getInstance(m_algorithm, m_algorithmProvider);
    cipher.init(Cipher.DECRYPT_MODE, m_secretKey, gcmSpec);
    return cipher.doFinal(encryptedData);
  }

  @Override
  public byte[] encryptProperty(final String property, final byte[] clearText, final Map<String, String> context) throws GeneralSecurityException {
    return encrypt(clearText, context);
  }

  @Override
  public byte[] decryptProperty(final String property, final byte[] encryptedData, final Map<String, String> context) throws GeneralSecurityException {
    return decrypt(encryptedData, context);
  }

  // === Configuration methods === //

  protected void assertConfiguration() {
    Assertions.assertNotNull(m_algorithmProvider, "'AES algorithm provider' not defined");
    Assertions.assertNotNull(m_pbeSalt, "'PBE salt' not defined");
    Assertions.assertNotNull(m_pbePassword, "'PBE password' not defined");
    Assertions.assertTrue(m_pbeKeyLength == 128 || m_pbeKeyLength == 192 || m_pbeKeyLength == 256, "'PBE key length' must be 128, 192 or 256 bits");
    Assertions.assertNotNull(m_pbeAlgorithm, "'PBE algorithm' not defined");
    Assertions.assertNotNull(m_pbeAlgorithmProvider, "'PBE algorithm provider' not defined");
    Assertions.assertGreater(m_pbeIterationCount, 0, "'PBE iteration count' not defined");
    Assertions.assertGreater(m_gcmInitializationVectorByteLength, 0, "GCM initialization vector' not defined");
    Assertions.assertGreater(m_gcmAuthTagBitLength, 0, "GCM auth tag length' not defined");
    Assertions.assertNotNull(m_padding, "'Padding' not defined");
  }

  protected void assertInitialized() {
    Assertions.assertTrue(m_initialized.get(), "{} not initialized", AesEncrypter.class);
  }

  protected void assertNotInitialized() {
    Assertions.assertFalse(m_initialized.get(), "{} already initialized", AesEncrypter.class);
  }

  /**
   * Specifies the provider to provide the 'AES' algorithm.
   */
  public AesEncrypter withAlgorithmProvider(final String algorithmProvider) {
    assertNotInitialized();
    m_algorithmProvider = algorithmProvider;
    return this;
  }

  /**
   * Specifies the salt to be used in password-based encryption (PBE) to derive the cryptographic shared key.
   * <p>
   * This salt is used in combination with the password and key-length to compute the derived cryptographic shared key.
   * <p>
   * Use {@link SecurityUtility#createSecureRandom()} in combination with {@link Base64Utility#encode(byte[])} to
   * generate a salt.
   */
  public AesEncrypter withPbeSalt(final byte[] pbeSalt) {
    assertNotInitialized();
    m_pbeSalt = pbeSalt;
    return this;
  }

  /**
   * Specifies the password to be used in password-based encryption (PBE) to derive the cryptographic shared key.
   * <p>
   * This password is used in combination with the salt and key-length to compute the derived cryptographic shared key.
   */
  public AesEncrypter withPbePassword(final char[] pbePassword) {
    assertNotInitialized();
    m_pbePassword = pbePassword;
    return this;
  }

  /**
   * Specifies the key length in bits of the cryptographic key to be generated. The key based on password and salt.
   * <p>
   * This key-length is used in combination with the salt and password to compute the derived cryptographic shared key.
   * <p>
   * Valid values are 128, 192 or 256.
   */
  public AesEncrypter withPbeKeyLength(final int pbeKeyLength) {
    assertNotInitialized();
    m_pbeKeyLength = pbeKeyLength;
    return this;
  }

  /**
   * Specifies the 'key-derivation' algorithm to be used in password-based encryption (PBE) to compute the cryptographic
   * shared key.
   */
  public AesEncrypter withPbeAlgorithm(final String pbeAlgorithm) {
    assertNotInitialized();
    m_pbeAlgorithm = pbeAlgorithm;
    return this;
  }

  /**
   * Specifies the provider to provide the algorithm specified via {@link #withPbeAlgorithm(String)}.
   */
  public AesEncrypter withPbeAlgorithmProvider(final String pbeAlgorithmProvider) {
    assertNotInitialized();
    m_pbeAlgorithmProvider = pbeAlgorithmProvider;
    return this;
  }

  /**
   * Specifies the number of times that the password is hashed during password-based encryption (PBE) to compute the
   * cryptographic shared key.
   */
  public AesEncrypter withPbeIterationCount(final int pbeIterationCount) {
    assertNotInitialized();
    m_pbeIterationCount = pbeIterationCount;
    return this;
  }

  /**
   * Specifies the length (bytes) of the initialization vector used for Galois/Counter operation mode.
   * <p>
   * GCM = Galois/Counter Mode, and provides both data authenticity (integrity) and confidentiality.
   *
   * @see http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf
   * @see GCMParameterSpec
   */
  public AesEncrypter withGcmInitializationVectorByteLength(final int gcmInitializationVectorByteLength) {
    assertNotInitialized();
    m_gcmInitializationVectorByteLength = gcmInitializationVectorByteLength;
    return this;
  }

  /**
   * Specifies the length (bits) of the authentication tag used for Galois/Counter operation mode.
   * <p>
   * The authentication tag is an input to the decryption and ensures data authenticity upon decryption.
   *
   * @see GCMParameterSpec
   * @see http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf
   */
  public AesEncrypter withGcmAuthTagBitLength(final int gcmAuthTagBitLength) {
    assertNotInitialized();
    m_gcmAuthTagBitLength = gcmAuthTagBitLength;
    return this;
  }

  /**
   * Specifies The padding scheme used by the cipher.
   */
  public AesEncrypter withPadding(final String padding) {
    assertNotInitialized();
    m_padding = padding;
    return this;
  }
}
