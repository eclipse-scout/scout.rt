/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.URL;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
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
   * Note: for backward compatibility use a key created with
   * {@link #createDecryptionKey(PushbackInputStream, char[], byte[], int, EncryptionKey)}
   */
  public static void decrypt(InputStream encryptedData, OutputStream clearTextData, EncryptionKey key) {
    PushbackInputStream input = new PushbackInputStream(encryptedData, 6);
    extractCompatibilityHeader(input);
    SECURITY_PROVIDER.get().decrypt(input, clearTextData, key);
  }

  /**
   * Creates a new {@link EncryptionKey} to be only used with
   * {@link #encrypt(InputStream, OutputStream, EncryptionKey)}.
   * <p>
   * Warning: This key must only be used for encryption. Decryption must be backwards compatible and uses meta
   * parameters stored at beginning of stream. Therefor never use an {@link EncryptionKey} directly to decrypt but
   * always with {@link SecurityUtility#decrypt(byte[], char[], byte[], int, EncryptionKey)}
   * <p>
   * See {@link ISecurityProvider#createEncryptionKey(char[], byte[], int)}
   */
  public static EncryptionKey createEncryptionKey(char[] password, byte[] salt, int keyLen) {
    return SECURITY_PROVIDER.get().createEncryptionKey(password, salt, keyLen);
  }

  /**
   * Creates a backward compatible {@link EncryptionKey} that is compatible to the encryptedData stream.
   * <p>
   * See {@link ISecurityProvider#createDecryptionKey(char[], byte[], int, byte[])}
   *
   * @param cipherStream
   *          must be capable to push back 8 bytes.
   * @param password
   *          The password to use to create the key. Must not be {@code null} or empty.
   * @param salt
   *          The salt to use for the key. Must not be {@code null} or empty. It is important to create a separate
   *          random salt for each key! Salts may not be shared by several keys. Use {@link #createRandomBytes(int)} to
   *          generate a new salt. It is safe to store the salt in clear text alongside the encrypted data.
   * @param keyLen
   *          The length of the key (in bits). Must be one of 128, 192 or 256.
   * @param optKey
   *          may be null. For performance optimization this may be pre-built key. However, in order to guarantee
   *          backward compatibility this optKey is only used when its parameters match those of the encrypted stream.
   *          See {@link EncryptionKey#getCompatibilityHeader()}
   * @return the key
   */
  public static EncryptionKey createDecryptionKey(PushbackInputStream cipherStream, char[] password, byte[] salt, int keyLen, EncryptionKey optKey) {
    byte[] compatibilityHeader = extractCompatibilityHeader(cipherStream);
    if (compatibilityHeader != null && optKey != null && optKey.getCompatibilityHeader() != null && Arrays.equals(optKey.getCompatibilityHeader(), compatibilityHeader)) {
      return optKey;
    }
    else {
      return SECURITY_PROVIDER.get().createDecryptionKey(password, salt, keyLen, compatibilityHeader);
    }
  }

  /**
   * After this call the {@link PushbackInputStream} starts at the encrypted data.
   *
   * @param cipherStream
   *          which can unread 6 charcters
   * @return extracted header <code>[yyyy:version]</code> or null of not found. yyyy is the 4-digit year and version is
   *         freetext without the ']' character
   */
  public static byte[] extractCompatibilityHeader(PushbackInputStream cipherStream) {
    try {
      byte[] first6 = cipherStream.readNBytes(6);
      if (first6.length == 6
          && first6[0] == '['
          && Character.isDigit(first6[1])
          && Character.isDigit(first6[2])
          && Character.isDigit(first6[3])
          && Character.isDigit(first6[4])
          && first6[5] == ':') {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        buf.write(first6);
        int b;
        while ((b = cipherStream.read()) >= 0) {
          buf.write(b);
          if (b == ']') {
            break;
          }
        }
        return buf.toByteArray();
      }
      //push back
      cipherStream.unread(first6);
      return null;
    }
    catch (IOException e) {
      throw new ProcessingException("Unable to decrypt data. Cannot read stream.", e);
    }
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
    Assertions.assertNotNull(clearTextData, "no data provided");
    ByteArrayInputStream input = new ByteArrayInputStream(clearTextData);
    int aesBlockSize = 16;
    int expectedOutSize = ((input.available() / aesBlockSize) + 2) * aesBlockSize;
    ByteArrayOutputStream result = new ByteArrayOutputStream(expectedOutSize);
    encrypt(input, result, key);
    return result.toByteArray();
  }

  /**
   * Note: for backward compatibility use a key created with
   * {@link #createDecryptionKey(PushbackInputStream, char[], byte[], int, EncryptionKey)}
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
    Assertions.assertNotNull(encryptedData, "no data provided");
    ByteArrayInputStream input = new ByteArrayInputStream(encryptedData);
    int expectedOutSize = input.available() - 8;
    ByteArrayOutputStream result = new ByteArrayOutputStream(expectedOutSize);
    decrypt(input, result, key);
    return result.toByteArray();
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
    return decrypt(encryptedData, password, salt, keyLen, null);
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
   * @param optKey
   *          may be null. For performance optimization this may be pre-built key. However, in order to guarantee
   *          backward compatibility this optKey is only used when its parameters match those of the encrypted stream.
   * @return The clear text bytes.
   * @throws AssertionException
   *           on invalid input
   * @throws ProcessingException
   *           if there is an error during decryption.
   */
  public static byte[] decrypt(byte[] encryptedData, char[] password, byte[] salt, int keyLen, EncryptionKey optKey) {
    Assertions.assertNotNull(encryptedData, "no data provided");
    PushbackInputStream input = new PushbackInputStream(new ByteArrayInputStream(encryptedData), 6);
    EncryptionKey key = createDecryptionKey(input, password, salt, keyLen, optKey);
    int expectedOutSize = encryptedData.length - 8;
    ByteArrayOutputStream result = new ByteArrayOutputStream(expectedOutSize);
    SECURITY_PROVIDER.get().decrypt(input, result, key);
    return result.toByteArray();
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
    EncryptionKey key = createEncryptionKey(password, salt, keyLen);
    return encrypt(clearTextData, key);
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
   * @see ISecurityProvider#createPasswordHash(char[], byte[])
   */
  public static byte[] hashPassword(char[] password, byte[] salt) {
    return SECURITY_PROVIDER.get().createPasswordHash(password, salt);
  }

  /**
   * This method is recommended in combination with {@link #hashPassword(char[], byte[])} where the iteration count is
   * omitted. This has the advantage that the check of the password hash is independent of the creation of the hash. In
   * case the iteration count is increased yearly, this method checks if the hash is valid
   *
   * @return true if calculated password hash created with {@link #hashPassword(char[], byte[])} matches the expected
   *         hash.
   * @since 11.0
   */
  public static boolean verifyPasswordHash(char[] password, byte[] salt, byte[] expectedHash) {
    return SECURITY_PROVIDER.get().verifyPasswordHash(password, salt, expectedHash);
  }

  /**
   * Creates a hash for the given data using the given salt.<br>
   * <br>
   * <b>Important:</b> For hashing of passwords use {@link #hashPassword(char[], byte[])}!
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
   * @see ISecurityProvider#createHash(InputStream, byte[], int)
   * @see ISecurityProvider#createPasswordHash(char[], byte[])
   */
  public static byte[] hash(byte[] data, byte[] salt) {
    Assertions.assertNotNull(data, "no data provided");
    return hash(new ByteArrayInputStream(data), salt);
  }

  /**
   * See {@link ISecurityProvider#createHash(InputStream, byte[], int)}
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
   * @param keyStorePath
   *          url
   * @param storePass
   *          keystore password
   * @param keyPass
   *          private key password. Optional.
   * @see #keyStoreToHumanReadableText(InputStream, String, String)
   * @since 10.0
   */
  public static String keyStoreToHumanReadableText(String keyStorePath, String storePass, String keyPass) {
    try (InputStream in = new URL(keyStorePath).openStream()) {
      return keyStoreToHumanReadableText(in, storePass, keyPass);
    }
    catch (IOException | RuntimeException e) {
      return "Error: " + e;
    }
  }

  /**
   * @param keyStoreInput
   *          stream
   * @param storePass
   *          keystore password
   * @param keyPass
   *          private key password. Optional.
   * @return human-readable text of the keystore content
   * @since 10.0
   */
  public static String keyStoreToHumanReadableText(InputStream keyStoreInput, String storePass, String keyPass) {
    return SECURITY_PROVIDER.get().keyStoreToHumanReadableText(keyStoreInput, storePass, keyPass);
  }

  @Override
  public String toString() {
    return SECURITY_PROVIDER.get().toString();
  }

  public static void main(String[] args) {
    // do not change the behavior of this method: it is part of the documentation that new key pairs should be generated using this main function.
    KeyPairBytes keyPair = generateKeyPair();
    System.out.format("base64 encoded key pair:%n  private key: %s%n  public key:  %s%n",
        Base64Utility.encode(keyPair.getPrivateKey()),
        Base64Utility.encode(keyPair.getPublicKey()));
  }
}
