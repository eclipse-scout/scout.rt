/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Utility class for encryption/decryption, hashing, random number generation and digital signatures.<br>
 * Please note that this class requires the following providers to be available and authenticated in the running JRE:
 * SUN, SunJCE, SunEC. See
 * <a href="http://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html#SunJCEProvider">Java
 * Cryptography Architecture Oracle Providers Documentation for JDK 8</a>.
 *
 * @since 5.1
 * @see Base64Utility
 */
public final class SecurityUtility {

  /**
   * Specifies the iteration count for hashing and encryption/decryption.
   */
  private static final int DEFAULT_CYCLES = 3557;

  /**
   * Number of random bytes to be created by default.
   */
  private static final int DEFAULT_RANDOM_SIZE = 16;

  /**
   * The Elliptic Curve Digital Signature Algorithm as defined in ANSI X9.62.
   */
  private static final String SIGNATURE_ALGORITHM = "SHA512withECDSA";

  /**
   * Generates keypairs for the Elliptic Curve algorithm.
   */
  private static final String ASYMMETRIC_KEY_ALGORITHM = "EC";

  /**
   * Koblitz curve secp256k1 as recommended by <a href="http://www.secg.org/sec2-v2.pdf">Standards for Efficient
   * Cryptography Group</a>.
   */
  private static final String ELLIPTIC_CURVE_NAME = "secp256k1";

  /**
   * SHA-512 hash algorithms defined in the
   * <a href="http://csrc.nist.gov/publications/fips/fips180-4/fips-180-4.pdf">FIPS PUB 180-4</a>.
   */
  private static final String MESSAGE_DIGEST_ALGORITHM = "SHA-512";

  /**
   * This algorithm uses SHA-1 as the foundation of the PRNG. It computes the SHA-1 hash over a true-random seed value
   * concatenated with a 64-bit counter which is incremented by 1 for each operation. From the 160-bit SHA-1 output,
   * only 64 bits are used.
   */
  private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";

  /**
   * Password-based key-derivation algorithm (<a href="http://tools.ietf.org/search/rfc2898">PKCS #5 2.0</a>) using The
   * HmacSHA algorithm (<a href="http://www.ietf.org/rfc/rfc2104.txt">RFC 2104</a>) as pseudo-random function.
   */
  private static final String SECRET_ALGORITHM = "PBKDF2WithHmacSHA256";

  /**
   * Java Cryptographic Extension Provider Name
   */
  private static final String JCE_PROVIDER = "SunJCE";

  /**
   * Sun provider for cipher instances
   */
  private static final String SUN_PROVIDER = "SUN";

  /**
   * Provider for the Elliptic Curve algorithm
   */
  private static final String EC_ALGORITHM_PROVIDER = "SunEC";

  /**
   * Advanced Encryption Standard as specified by
   * <a href="http://csrc.nist.gov/publications/fips/fips197/fips-197.pdf">NIST in FIPS 197</a>. Also known as the
   * Rijndael algorithm by Joan Daemen and Vincent Rijmen. AES is a 128-bit block cipher.
   */
  private static final String CIPHER_ALGORITHM = "AES";

  /**
   * Galois/Counter Mode (as defined in <a href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST
   * Special Publication SP 800-38D</a>).
   */
  private static final String CIPHER_ALGORITHM_MODE = "GCM";

  /**
   * PKCS5 padding scheme (as defined in <a href="http://tools.ietf.org/html/rfc2898">PKCS #5</a>).
   */
  private static final String CIPHER_ALGORITHM_PADDING = "PKCS5Padding";

  /**
   * Length (in bytes) of Initialization Vector for Galois/Counter Mode (as defined in
   * <a href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST Special Publication SP 800-38D</a>
   * ).
   */
  private static final int GCM_INITIALIZATION_VECTOR_LEN = 16;

  /**
   * Length (in bits) of authentication tag T of Initialization Vector for Galois/Counter Mode (as defined in
   * <a href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST Special Publication SP 800-38D</a>
   * ).
   */
  private static final int GCM_AUTH_TAG_BIT_LEN = 128;

  private SecurityUtility() {
  }

  /**
   * Decrypts the data using the given key and salt.<br>
   * This method is intended to decrypt data that was previously encrypted using
   * {@link #encrypt(byte[], String, byte[], int)}.<br>
   * <br>
   * <b>Note:</b><br>
   * It uses AES (as defined in <a href="http://csrc.nist.gov/publications/fips/fips197/fips-197.pdf">NIST FIPS 197</a>)
   * in Galois/Counter Mode (as defined in
   * <a href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST Special Publication SP 800-38D</a>
   * ) with PKCS5 padding scheme (as defined in <a href="http://tools.ietf.org/html/rfc2898">PKCS #5</a>) to decrypt the
   * data.<br>
   * This algorithms is only available with JRE 1.8 or newer. To have the best security ensure to use the latest JRE
   * 1.8!
   *
   * @param encryptedData
   *          The encrypted data that should be decrypted.
   * @param password
   *          The password to use for the decryption. Must not be null or empty.
   * @param salt
   *          The salt to use for the decryption. This is the same salt that was used to encrypt the data.
   * @param keyLen
   *          The key length (in bits) to use. Valid values are 128, 192, or 256. <b>Note:</b> To use key lengths other
   *          than 128bits the "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files" must be
   *          installed in the JRE (see
   *          <a href="http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html">Java
   *          Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 8 Download</a>).
   * @return The original clear text data.
   * @throws ProcessingException
   *           If there is an error during decryption.
   * @throws IllegalArgumentException
   *           If the salt or key is null or empty or an unsupported keyLen has been provided.
   */
  public static byte[] decrypt(byte[] encryptedData, String password, byte[] salt, int keyLen) {
    return doCrypt(encryptedData, password, salt, Cipher.DECRYPT_MODE, keyLen);
  }

  /**
   * Encrypts the given data using the given key and salt.<br>
   * Use {@link #decrypt(byte[], String, byte[], int)} to decrypt the data again (using the same key, salt and keyLen).
   * <br>
   * <br>
   * <b>Note:</b><br>
   * It uses AES (as defined in <a href="http://csrc.nist.gov/publications/fips/fips197/fips-197.pdf">NIST FIPS 197</a>)
   * in Galois/Counter Mode (as defined in
   * <a href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST Special Publication SP 800-38D</a>
   * ) with PKCS5 padding scheme (as defined in <a href="http://tools.ietf.org/html/rfc2898">PKCS #5</a>) to encrypt the
   * data.<br>
   * This algorithms is only available with JRE 1.8 or newer. To have the best security ensure to use the latest JRE
   * 1.8!
   *
   * @param clearTextData
   *          The data to encrypt.
   * @param password
   *          The password to use for the encryption. Must not be null or empty.
   * @param salt
   *          The salt to use for the encryption. Must not be null or empty. It is important to create a separate random
   *          salt for each key! Salts may not be shared by several keys. Use {@link #createRandomBytes()} to generate a
   *          new salt for a key. It is safe to store the salt in clear text alongside the encrypted data. This salt
   *          will then be used to decrypt the data again.
   * @param keyLen
   *          The key length (in bits) to use. Valid values are 128, 192, or 256. <b>Note:</b> To use key lengths other
   *          than 128bits the "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files" must be
   *          installed in the JRE (see
   *          <a href="http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html">Java
   *          Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 8 Download</a>).
   * @return The encrypted data.
   * @throws ProcessingException
   *           If there is an error during encryption.
   * @throws IllegalArgumentException
   *           If the salt or key is null or empty or an unsupported keyLen has been provided.
   */
  public static byte[] encrypt(byte[] clearTextData, String password, byte[] salt, int keyLen) {
    return doCrypt(clearTextData, password, salt, Cipher.ENCRYPT_MODE, keyLen);
  }

  protected static byte[] doCrypt(byte[] input, String password, byte[] salt, int mode, int keyLen) {
    if (input == null || input.length < 1) {
      return input;
    }
    if (StringUtility.isNullOrEmpty(password)) {
      throw new IllegalArgumentException("key must not be empty.");
    }
    if (salt == null || salt.length < 1) {
      throw new IllegalArgumentException("salt must be provided.");
    }
    if (keyLen != 128 && keyLen != 192 && keyLen != 256) {
      throw new IllegalArgumentException("key length must be 128, 192 or 256.");
    }

    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_ALGORITHM, JCE_PROVIDER);
      KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, DEFAULT_CYCLES, keyLen + (GCM_INITIALIZATION_VECTOR_LEN * 8));
      SecretKey tmp = factory.generateSecret(spec);

      // derive Key and Initialization Vector
      byte[] encoded = tmp.getEncoded();
      byte[] iv = new byte[GCM_INITIALIZATION_VECTOR_LEN];
      byte[] key = new byte[keyLen / 8];
      System.arraycopy(encoded, 0, key, 0, key.length);
      System.arraycopy(encoded, key.length, iv, 0, GCM_INITIALIZATION_VECTOR_LEN);

      SecretKey secret = new SecretKeySpec(key, CIPHER_ALGORITHM);
      GCMParameterSpec parameters = new GCMParameterSpec(GCM_AUTH_TAG_BIT_LEN, iv);

      Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM + "/" + CIPHER_ALGORITHM_MODE + "/" + CIPHER_ALGORITHM_PADDING, JCE_PROVIDER);
      cipher.init(mode, secret, parameters);

      return cipher.doFinal(input);
    }
    catch (NoSuchAlgorithmException e) {
      throw new ProcessingException("Unable to crypt data. Algorithm could not be found. Make sure to use JRE 1.8.0_20 or newer.", e);
    }
    catch (InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
      throw new ProcessingException("Unable to crypt data.", e);
    }
  }

  /**
   * Generates random bytes. The number of bytes is defined by {@link #DEFAULT_RANDOM_SIZE}.<br>
   * This algorithm uses SHA-1 as the foundation of the PRNG. It computes the SHA-1 hash over a true-random seed value
   * concatenated with a 64-bit counter which is incremented by 1 for each operation.
   *
   * @return the created random bytes.
   * @throws ProcessingException
   *           If the current platform does not support the SHA1PRNG number generation algorithm.
   */
  public static byte[] createRandomBytes() {
    return createRandomBytes(DEFAULT_RANDOM_SIZE);
  }

  /**
   * Generates a user-specified number of random bytes.<br>
   * This algorithm uses SHA-1 as the foundation of the PRNG. It computes the SHA-1 hash over a true-random seed value
   * concatenated with a 64-bit counter which is incremented by 1 for each operation.
   *
   * @param size
   *          The number of bytes to create.
   * @return the created random bytes.
   * @throws ProcessingException
   *           If the current platform does not support the SHA1PRNG number generation algorithm.
   * @throws IllegalArgumentException
   *           if the size is less than 1.
   */
  public static byte[] createRandomBytes(int size) {
    if (size < 1) {
      throw new IllegalArgumentException(size + " is not a valid number for random bytes.");
    }
    try {
      SecureRandom random = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM, SUN_PROVIDER);
      byte[] rnd = new byte[size];
      random.nextBytes(rnd);
      return rnd;
    }
    catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new ProcessingException("Unable to create random number.", e);
    }
  }

  /**
   * Creates a SHA-512 hash using the given data input and salt.
   *
   * @param data
   *          the data to hash
   * @param salt
   *          the salt to use. Use {@link #createRandomBytes()} to generate a random salt per instance.
   * @return the hash
   * @throws ProcessingException
   *           If the current platform does not support the SHA-512 algorithm.
   * @see <a href="http://csrc.nist.gov/publications/fips/fips180-4/fips-180-4.pdf">FIPS PUB 180-4</a>
   */
  public static byte[] hash(byte[] data, byte[] salt) {
    return hash(data, salt, DEFAULT_CYCLES);
  }

  /**
   * Creates a SHA-512 hash using the given data input and salt.
   *
   * @param data
   *          the data to hash
   * @param salt
   *          the salt to use. Use {@link #createRandomBytes()} to generate a random salt per instance.
   * @param iterations
   *          the number of cycles to hash. There is always at least one cycle executed.
   * @return the hash
   * @throws ProcessingException
   *           If the current platform does not support the SHA-512 algorithm.
   * @throws IllegalArgumentException
   *           If data is null.
   * @see <a href="http://csrc.nist.gov/publications/fips/fips180-4/fips-180-4.pdf">FIPS PUB 180-4</a>
   */
  public static byte[] hash(byte[] data, byte[] salt, int iterations) {
    if (data == null) {
      throw new IllegalArgumentException("no data provided");
    }
    try {
      MessageDigest digest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM, SUN_PROVIDER);
      digest.reset();
      if (salt != null && salt.length > 0) {
        digest.update(salt);
      }

      byte[] key = digest.digest(data);
      for (int i = 1; i < iterations; i++) {
        digest.reset();
        key = digest.digest(key);
      }
      return key;
    }
    catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new ProcessingException("Unable to hash.", e);
    }
  }

  /**
   * Creates a new key pair (private and public key).
   *
   * @return The new {@link KeyPairBytes}.
   * @throws ProcessingException
   *           When there is an error generating the new keys.
   */
  public static KeyPairBytes generateKeyPair() {
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ASYMMETRIC_KEY_ALGORITHM, EC_ALGORITHM_PROVIDER);
      SecureRandom random = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM, SUN_PROVIDER);
      ECGenParameterSpec spec = new ECGenParameterSpec(ELLIPTIC_CURVE_NAME);
      keyGen.initialize(spec, random);
      KeyPair keyPair = keyGen.generateKeyPair();

      X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
      PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());

      return new KeyPairBytes(pkcs8EncodedKeySpec.getEncoded(), x509EncodedKeySpec.getEncoded());
    }
    catch (NoSuchProviderException | InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
      throw new ProcessingException("unable to create a new key-pair", e);
    }
  }

  /**
   * Creates a signature for the given data using the given private key.<br>
   * Compatible keys can be generated using {@link #generateKeyPair()}.
   *
   * @param privateKey
   *          The private key bytes in PKCS#8 encoding.
   * @param data
   *          The data for which the signature should be created.
   * @return The signature bytes.
   * @throws ProcessingException
   *           When there is an error creating the signature.
   */
  public static byte[] createSignature(byte[] privateKey, byte[] data) {
    try {
      // create private key from bytes
      KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_KEY_ALGORITHM, EC_ALGORITHM_PROVIDER);
      PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKey);
      PrivateKey priv = keyFactory.generatePrivate(privateKeySpec);

      // create signature
      Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM, EC_ALGORITHM_PROVIDER);
      SecureRandom random = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM, SUN_PROVIDER);
      sig.initSign(priv, random);
      sig.update(data);
      return sig.sign();
    }
    catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
      throw new ProcessingException("unable to create signature.", e);
    }
  }

  /**
   * Verifies the given signature for the given data and public key.<br>
   * Compatible public keys can be generated using {@link #generateKeyPair()}.
   *
   * @param publicKey
   *          The public key bytes as defined in the X.509 standard.
   * @param data
   *          The data for which the signature should be validated.
   * @param signatureToVerify
   *          The signature that should be verified against.
   * @return <code>true</code> if the given signature is valid for the given data and public key. <code>false</code>
   *         otherwise.
   * @throws ProcessingException
   *           When there is an error validating the signature.
   */
  public static boolean verifySignature(byte[] publicKey, byte[] data, byte[] signatureToVerify) {
    try {
      // create public key from bytes
      KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_KEY_ALGORITHM, EC_ALGORITHM_PROVIDER);
      X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKey);
      PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

      // verify signature
      Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM, EC_ALGORITHM_PROVIDER);
      sig.initVerify(pubKey);
      sig.update(data);
      return sig.verify(signatureToVerify);
    }
    catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException | InvalidKeyException | SignatureException t) {
      throw new ProcessingException("unable to verify signature", t);
    }
  }

  /**
   * Generates a new base64 encoded key pair and prints it on standard out.
   */
  public static void main(String[] args) {
    KeyPairBytes keyPair = generateKeyPair();
    System.out.format("base64 encoded key pair:\n  priavte key: %s\n  public key:  %s\n",
        Base64Utility.encode(keyPair.getPrivateKey()),
        Base64Utility.encode(keyPair.getPublicKey()));
  }

  /**
   * Public and private key bytes.
   */
  public static final class KeyPairBytes {

    private final byte[] m_privateKey;
    private final byte[] m_publicKey;

    private KeyPairBytes(byte[] priv, byte[] pub) {
      m_privateKey = priv;
      m_publicKey = pub;
    }

    /**
     * Gets the private key bytes in PKCS#8 encoding.
     *
     * @return The private key.
     */
    public byte[] getPrivateKey() {
      return Arrays.copyOf(m_privateKey, m_privateKey.length);
    }

    /**
     * Gets the public key bytes as defined in the X.509 standard.
     *
     * @return The public key.
     */
    public byte[] getPublicKey() {
      return Arrays.copyOf(m_publicKey, m_publicKey.length);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(m_privateKey);
      result = prime * result + Arrays.hashCode(m_publicKey);
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof KeyPairBytes)) {
        return false;
      }
      KeyPairBytes other = (KeyPairBytes) obj;
      if (!Arrays.equals(m_privateKey, other.m_privateKey)) {
        return false;
      }
      if (!Arrays.equals(m_publicKey, other.m_publicKey)) {
        return false;
      }
      return true;
    }
  }
}
