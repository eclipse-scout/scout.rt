/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Utility class for secure encryption/decryption, hashing and random number generation.
 *
 * @since 5.1
 * @see Base64Utility
 */
public final class EncryptionUtility2 {

  /**
   * Specifies the iteration count for hashing and encryption/decryption.
   */
  public static int CYCLES = 3557;

  /**
   * Number of random bytes to be created by default.
   */
  public static int DEFAULT_RANDOM_SIZE = 16;

  //
  //
  // encryption/decryption configuration:

  /**
   * Password-based key-derivation algorithm (<a href="http://tools.ietf.org/search/rfc2898">PKCS #5 2.0</a>) using
   * The HmacSHA algorithm (<a href="http://www.ietf.org/rfc/rfc2104.txt">RFC 2104</a>) as pseudo-random function.
   */
  private static final String SECRET_ALGORITHM = "PBKDF2WithHmacSHA256";

  /**
   * Advanced Encryption Standard as specified by <a
   * href="http://csrc.nist.gov/publications/fips/fips197/fips-197.pdf">NIST in FIPS 197</a>. Also known as the Rijndael
   * algorithm by Joan Daemen and Vincent Rijmen. AES is a 128-bit block cipher.
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
   * Length (in bytes) of Initialization Vector for Galois/Counter Mode (as defined in <a
   * href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST Special Publication SP 800-38D</a>).
   */
  private static final int GCM_INITIALIZATION_VECTOR_LEN = 16;

  /**
   * Length (in bits) of authentication tag T of Initialization Vector for Galois/Counter Mode (as defined in <a
   * href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST Special Publication SP 800-38D</a>).
   */
  private static final int GCM_AUTH_TAG_BIT_LEN = 128;

  private EncryptionUtility2() {
  }

  /**
   * Decrypts the data using the given key and salt.<br>
   * This method is intended to decrypt data that was previously encrypted using
   * {@link #encrypt(byte[], String, byte[], int)}.<br>
   * <br>
   * <b>Note:</b><br>
   * It uses AES (as defined in <a href="http://csrc.nist.gov/publications/fips/fips197/fips-197.pdf">NIST FIPS
   * 197</a>) in Galois/Counter Mode (as defined in <a
   * href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST Special Publication SP 800-38D</a>)
   * with PKCS5 padding scheme (as defined in <a href="http://tools.ietf.org/html/rfc2898">PKCS #5</a>) to decrypt the
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
   *          installed in the JRE (see <a
   *          href="http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html">Java
   *          Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 8 Download</a>).
   * @return The original clear text data.
   * @throws ProcessingException
   *           If there is an error during decryption.
   * @throws IllegalArgumentException
   *           If the salt or key is null or empty or an unsupported keyLen has been provided.
   */
  public static byte[] decrypt(byte[] encryptedData, String password, byte[] salt, int keyLen) throws ProcessingException {
    return doCrypt(encryptedData, password, salt, Cipher.DECRYPT_MODE, keyLen);
  }

  /**
   * Encrypts the given data using the given key and salt.<br>
   * Use {@link #decrypt(byte[], String, byte[], int)} to decrypt the data again (using the same key, salt and keyLen).<br>
   * <br>
   * <b>Note:</b><br>
   * It uses AES (as defined in <a href="http://csrc.nist.gov/publications/fips/fips197/fips-197.pdf">NIST FIPS
   * 197</a>) in Galois/Counter Mode (as defined in <a
   * href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST Special Publication SP 800-38D</a>)
   * with PKCS5 padding scheme (as defined in <a href="http://tools.ietf.org/html/rfc2898">PKCS #5</a>) to encrypt the
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
   *          salt for each key! Salts may not be shared by several keys. Use {@link #createRandomBytes()} to
   *          generate a new salt for a key. It is safe to store the salt in clear text alongside the encrypted data.
   *          This salt will then be used to decrypt the data again.
   * @param keyLen
   *          The key length (in bits) to use. Valid values are 128, 192, or 256. <b>Note:</b> To use key lengths other
   *          than 128bits the "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files" must be
   *          installed in the JRE (see <a
   *          href="http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html">Java
   *          Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 8 Download</a>).
   * @return The encrypted data.
   * @throws ProcessingException
   *           If there is an error during encryption.
   * @throws IllegalArgumentException
   *           If the salt or key is null or empty or an unsupported keyLen has been provided.
   */
  public static byte[] encrypt(byte[] clearTextData, String password, byte[] salt, int keyLen) throws ProcessingException {
    return doCrypt(clearTextData, password, salt, Cipher.ENCRYPT_MODE, keyLen);
  }

  protected static byte[] doCrypt(byte[] input, String password, byte[] salt, int mode, int keyLen) throws ProcessingException {
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
      SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_ALGORITHM);
      KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, CYCLES, keyLen + (GCM_INITIALIZATION_VECTOR_LEN * 8));
      SecretKey tmp = factory.generateSecret(spec);

      // derive Key and Initialization Vector
      byte[] encoded = tmp.getEncoded();
      byte[] iv = new byte[GCM_INITIALIZATION_VECTOR_LEN];
      byte[] key = new byte[keyLen / 8];
      System.arraycopy(encoded, 0, key, 0, key.length);
      System.arraycopy(encoded, key.length, iv, 0, GCM_INITIALIZATION_VECTOR_LEN);

      SecretKey secret = new SecretKeySpec(key, CIPHER_ALGORITHM);
      GCMParameterSpec parameters = new GCMParameterSpec(GCM_AUTH_TAG_BIT_LEN, iv);

      Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM + "/" + CIPHER_ALGORITHM_MODE + "/" + CIPHER_ALGORITHM_PADDING);
      cipher.init(mode, secret, parameters);

      return cipher.doFinal(input);
    }
    catch (NoSuchAlgorithmException e) {
      throw new ProcessingException("Unable to crypt data. Algorithm could not be found. Make sure to use JRE 1.8.0_20 or newer.", e);
    }
    catch (InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
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
  public static byte[] createRandomBytes() throws ProcessingException {
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
  public static byte[] createRandomBytes(int size) throws ProcessingException {
    if (size < 1) {
      throw new IllegalArgumentException(size + " is not a valid number for random bytes.");
    }
    try {
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      byte[] rnd = new byte[size];
      random.nextBytes(rnd);
      return rnd;
    }
    catch (NoSuchAlgorithmException e) {
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
  public static byte[] hash(byte[] data, byte[] salt) throws ProcessingException {
    return hash(data, salt, CYCLES);
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
  public static byte[] hash(byte[] data, byte[] salt, int iterations) throws ProcessingException {
    if (data == null) {
      throw new IllegalArgumentException("no data provided");
    }
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-512");
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
    catch (NoSuchAlgorithmException e) {
      throw new ProcessingException("Unable to hash.", e);
    }
  }
}
