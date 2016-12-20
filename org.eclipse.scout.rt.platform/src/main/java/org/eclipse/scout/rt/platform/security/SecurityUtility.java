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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.HexUtility;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Utility class for encryption & decryption, hashing and creation of random numbers, message authentication codes and
 * digital signatures.<br>
 * The {@link Base64Utility} or {@link HexUtility} can be used to encode the bytes returned by this class.
 *
 * @since 5.1
 * @see Base64Utility
 * @see HexUtility
 * @see ISecurityProvider
 */
public final class SecurityUtility {

  /**
   * Number of random bytes to be created by default.
   */
  private static final int DEFAULT_RANDOM_SIZE = 32;

  private static final LazyValue<ISecurityProvider> SECURITY_PROVIDER = new LazyValue<>(ISecurityProvider.class);

  private SecurityUtility() {
    // no instances of this class
  }

  /**
   * See {@link ISecurityProvider#encrypt(InputStream, OutputStream, EncryptionKey)}
   */
  public static void encrypt(InputStream clearTextData, OutputStream encryptedData, EncryptionKey key) {
    SECURITY_PROVIDER.get().encrypt(clearTextData, encryptedData, key);
  }

  /**
   * See {@link ISecurityProvider#decrypt(InputStream, OutputStream, EncryptionKey)}
   */
  public static void decrypt(InputStream encryptedData, OutputStream clearTextData, EncryptionKey key) {
    SECURITY_PROVIDER.get().decrypt(encryptedData, clearTextData, key);
  }

  /**
   * See {@link ISecurityProvider#createEncryptionKey(char[], byte[], int)}
   */
  public static EncryptionKey createEncryptionKey(char[] password, byte[] salt, int keyLen) {
    return SECURITY_PROVIDER.get().createEncryptionKey(password, salt, keyLen);
  }

  /**
   * Encrypts the given bytes using the given {@link EncryptionKey}.<br>
   * Use {@link #createEncryptionKey(char[], byte[], int)} to create a key instance.
   *
   * @param clearTextData
   *          The clear text data. Must not be {@code null}.
   * @param key
   *          The {@link EncryptionKey} to use. Must not be {@code null}.
   * @return The encrypted bytes
   * @throws AssertionException
   *           on invalid input.
   * @throws ProcessingException
   *           if there is an error during encryption.
   */
  public static byte[] encrypt(byte[] clearTextData, EncryptionKey key) {
    return doCrypt(clearTextData, key, true);
  }

  /**
   * Decrypts the given bytes using the given {@link EncryptionKey}.<br>
   * Use {@link #createEncryptionKey(char[], byte[], int)} to create a key instance.
   *
   * @param encryptedData
   *          The encrypted bytes. Must not be {@code null}.
   * @param key
   *          The {@link EncryptionKey} to use. Must not be {@code null}.
   * @return The clear text data.
   * @throws AssertionException
   *           on invalid input
   * @throws ProcessingException
   *           if there is an error during decryption.
   */
  public static byte[] decrypt(byte[] encryptedData, EncryptionKey key) {
    return doCrypt(encryptedData, key, false);
  }

  /**
   * Encrypts the given clear text bytes using the given password and salt.
   *
   * @param clearTextData
   *          The clear text data. Must not be {@code null}.
   * @param password
   *          The password to use to create the key. Must not be {@code null} or empty.
   * @param salt
   *          The salt to use for the key. Must not be {@code null} or empty. It is important to create a separate
   *          random salt for each key! Salts may not be shared by several keys. Use {@link #createRandomBytes(int)} to
   *          generate a new salt. It is safe to store the salt in clear text alongside the encrypted data.
   * @param keyLen
   *          The length of the key (in bits). Must be one of 128, 192 or 256.
   * @return The encrypted bytes.
   * @throws AssertionException
   *           on invalid input
   * @throws ProcessingException
   *           if there is an error during encryption.
   */
  public static byte[] encrypt(byte[] clearTextData, char[] password, byte[] salt, int keyLen) {
    return doCrypt(clearTextData, password, salt, keyLen, true);
  }

  /**
   * @param encryptedData
   *          The encrypted bytes. Must not be {@code null}.
   * @param password
   *          The password to use to create the key. Must not be {@code null} or empty.
   * @param salt
   *          The salt to use for the key. Must not be {@code null} or empty. It is important to create a separate
   *          random salt for each key! Salts may not be shared by several keys. Use {@link #createRandomBytes(int)} to
   *          generate a new salt. It is safe to store the salt in clear text alongside the encrypted data.
   * @param keyLen
   *          The length of the key (in bits). Must be one of 128, 192 or 256.
   * @return The clear text bytes.
   * @throws AssertionException
   *           on invalid input
   * @throws ProcessingException
   *           if there is an error during decryption.
   */
  public static byte[] decrypt(byte[] encryptedData, char[] password, byte[] salt, int keyLen) {
    return doCrypt(encryptedData, password, salt, keyLen, false);
  }

  static byte[] doCrypt(byte[] data, char[] password, byte[] salt, int keyLen, boolean encrypt) {
    EncryptionKey key = createEncryptionKey(password, salt, keyLen);
    return doCrypt(data, key, encrypt);
  }

  static byte[] doCrypt(byte[] data, EncryptionKey key, boolean encrypt) {
    Assertions.assertNotNull(data, "no data provided");

    // no need to close ByteArray-Streams
    ByteArrayInputStream input = new ByteArrayInputStream(data);
    int expectedOutSize = input.available();
    int aesBlockSize = 16;
    if (encrypt) {
      expectedOutSize = ((expectedOutSize / aesBlockSize) + 2) * aesBlockSize;
    }
    else {
      expectedOutSize -= 8;
    }
    ByteArrayOutputStream result = new ByteArrayOutputStream(expectedOutSize);
    if (encrypt) {
      encrypt(input, result, key);
    }
    else {
      decrypt(input, result, key);
    }
    return result.toByteArray();
  }

  /**
   * See {@link ISecurityProvider#createSecureRandomBytes(int)}
   */
  public static byte[] createRandomBytes(int numBytes) {
    return SECURITY_PROVIDER.get().createSecureRandomBytes(numBytes);
  }

  /**
   * Generates 32 random bytes.
   *
   * @return the created random bytes.
   * @throws ProcessingException
   *           If the current platform does not support the random number generation algorithm.
   * @see ISecurityProvider#createSecureRandomBytes(int)
   */
  public static byte[] createRandomBytes() {
    return createRandomBytes(DEFAULT_RANDOM_SIZE);
  }

  /**
   * See {@link ISecurityProvider#createSecureRandom()}
   */
  public static SecureRandom createSecureRandom() {
    return SECURITY_PROVIDER.get().createSecureRandom();
  }

  /**
   * @see ISecurityProvider#createPasswordHash(char[], byte[], int)
   */
  public static byte[] hashPassword(char[] password, byte[] salt, int iterations) {
    return SECURITY_PROVIDER.get().createPasswordHash(password, salt, iterations);
  }

  /**
   * Creates a hash for the given data using the given salt.<br>
   * <br>
   * <b>Important:</b> For hashing of passwords use {@link #hashPassword(char[], byte[], int)}!
   *
   * @param data
   *          The data to hash. Must not be {@code null}.
   * @param salt
   *          The salt to use. Use {@link #createRandomBytes(int)} to generate a random salt per instance.
   * @return the hash
   * @throws ProcessingException
   *           If there is an error creating the hash
   * @throws AssertionException
   *           If data is {@code null}.
   * @see ISecurityProvider#createHash(InputStream, byte[])
   * @see ISecurityProvider#createPasswordHash(char[], byte[], int)
   */
  public static byte[] hash(byte[] data, byte[] salt) {
    Assertions.assertNotNull(data, "no data provided");
    return hash(new ByteArrayInputStream(data), salt);
  }

  /**
   * See {@link ISecurityProvider#createHash(InputStream, byte[])}
   */
  public static byte[] hash(InputStream data, byte[] salt) {
    return SECURITY_PROVIDER.get().createHash(data, salt, 3557 /* number of default cycles for backwards compatibility */);
  }

  /**
   * See {@link ISecurityProvider#createKeyPair()}
   */
  public static KeyPairBytes generateKeyPair() {
    return SECURITY_PROVIDER.get().createKeyPair();
  }

  /**
   * See {@link ISecurityProvider#createSignature(byte[], InputStream)}
   */
  public static byte[] createSignature(byte[] privateKey, InputStream data) {
    return SECURITY_PROVIDER.get().createSignature(privateKey, data);
  }

  /**
   * Creates a signature for the given data using the given private key.<br>
   * Compatible keys can be generated using {@link #generateKeyPair()}.
   *
   * @param privateKey
   *          The private key bytes.
   * @param data
   *          The data for which the signature should be created.
   * @return The signature bytes.
   * @throws ProcessingException
   *           When there is an error creating the signature.
   * @throws AssertionException
   *           if the private key or data is {@code null}.
   * @see ISecurityProvider#createSignature(byte[], InputStream)
   */
  public static byte[] createSignature(byte[] privateKey, byte[] data) {
    Assertions.assertNotNull(data, "no data provided");
    return createSignature(privateKey, new ByteArrayInputStream(data));
  }

  /**
   * See {@link ISecurityProvider#verifySignature(byte[], InputStream, byte[])}
   */
  public static boolean verifySignature(byte[] publicKey, InputStream data, byte[] signatureToVerify) {
    return SECURITY_PROVIDER.get().verifySignature(publicKey, data, signatureToVerify);
  }

  /**
   * Verifies the given signature for the given data and public key.<br>
   * Compatible public keys can be generated using {@link #generateKeyPair()}.
   *
   * @param publicKey
   *          The public key bytes.
   * @param data
   *          The data for which the signature should be validated. Must not be {@code null}
   * @param signatureToVerify
   *          The signature that should be verified against.
   * @return {@code true} if the given signature is valid for the given data and public key. {@code false} otherwise.
   * @throws ProcessingException
   *           If there is an error validating the signature.
   * @throws AssertionException
   *           if one of the arguments is {@code null}.
   * @see ISecurityProvider#verifySignature(byte[], InputStream, byte[])
   */
  public static boolean verifySignature(byte[] publicKey, byte[] data, byte[] signatureToVerify) {
    Assertions.assertNotNull(data, "no data provided");
    return verifySignature(publicKey, new ByteArrayInputStream(data), signatureToVerify);
  }

  /**
   * See {@link ISecurityProvider#createMac(byte[], InputStream)}
   */
  public static byte[] createMac(byte[] password, InputStream data) {
    return SECURITY_PROVIDER.get().createMac(password, data);
  }

  /**
   * Create a Message Authentication Code (MAC) for the given data and password.
   *
   * @param password
   *          The password to create the authentication code.
   * @param data
   *          The data for which the code should be created. Must not be {@code null}.
   * @return The created authentication code.
   * @throws ProcessingException
   *           if there is an error creating the MAC
   * @throws AssertionException
   *           if the password or data is {@code null}.
   * @see ISecurityProvider#createMac(byte[], InputStream)
   */
  public static byte[] createMac(byte[] password, byte[] data) {
    Assertions.assertNotNull(data, "no data provided");
    return createMac(password, new ByteArrayInputStream(data));
  }

  /**
   * @return the principal names of the given {@link Subject}, or {@code null} if the given {@link Subject} is
   *         {@code null}. Multiple principal names are separated by comma.
   */
  public static String getPrincipalNames(Subject subject) {
    if (subject == null) {
      return null;
    }

    Set<Principal> principals = subject.getPrincipals();
    final List<String> principalNames = new ArrayList<>(principals.size());
    for (final Principal principal : principals) {
      principalNames.add(principal.getName());
    }
    return StringUtility.join(", ", principalNames);
  }

  /**
   * Generates a new base64 encoded key pair and prints it on standard out.
   */
  public static void main(String[] args) {
    KeyPairBytes keyPair = generateKeyPair();
    System.out.format("base64 encoded key pair:%n  private key: %s%n  public key:  %s%n",
        Base64Utility.encode(keyPair.getPrivateKey()),
        Base64Utility.encode(keyPair.getPublicKey()));
  }
}
