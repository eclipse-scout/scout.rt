/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.SecureRandom;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Provider class for encryption & decryption, hashing and creation of random numbers, message authentication codes and
 * digital signatures.
 *
 * @since 6.1
 */
@ApplicationScoped
public interface ISecurityProvider {

  /**
   * Specifies the minimum of password hash iterations with PBKDF2-HMAC-SHA512.
   * <p>
   * <a href="https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html">PBKDF2</a>
   */
  int MIN_PASSWORD_HASH_ITERATIONS_2016 = 10000;
  int MIN_PASSWORD_HASH_ITERATIONS_2019 = 20000;
  int MIN_PASSWORD_HASH_ITERATIONS_2021 = 120000;
  int MIN_PASSWORD_HASH_ITERATIONS_2023 = 210000;
  int MIN_PASSWORD_HASH_ITERATIONS = MIN_PASSWORD_HASH_ITERATIONS_2023;

  /**
   * <pre>
     secretKeyAlgorithm: PBKDF2WithHmacSHA256
     cipherAlgorithm/Provider: AES/SunJCE
     GCM init vector length: 16
     GCM auth tag bit length: 128
     key derivation iteration count: 3557
   * </pre>
   */
  String ENCRYPTION_COMPATIBILITY_HEADER_2021_V1 = "[2021:v1]";
  /**
   * <pre>
   secretKeyAlgorithm: PBKDF2WithHmacSHA256
   cipherAlgorithm/Provider: AES/SunJCE
   GCM init vector length: 16
   GCM auth tag bit length: 128
   key derivation iteration count: 3557
   * </pre>
   */
  String ENCRYPTION_COMPATIBILITY_HEADER_2023_V1 = "[2023:v1]";
  String ENCRYPTION_COMPATIBILITY_HEADER = ENCRYPTION_COMPATIBILITY_HEADER_2023_V1;

  /**
   * Create a Message Authentication Code (MAC) for the given data and password.
   *
   * @param password
   *          The password to create the authentication code. Must not be {@code null} or empty.
   * @param data
   *          The {@link InputStream} that provides the data for which the MAC should be created.
   * @return The created authentication code.
   * @throws ProcessingException
   *           if there is an error creating the MAC
   * @throws AssertionException
   *           if the password is {@code null} or empty or if the data is {@code null}.
   */
  byte[] createMac(byte[] password, InputStream data);

  /**
   * Creates a signature for the given data using the given private key.<br>
   * Compatible keys can be generated using {@link #createKeyPair()}.
   *
   * @param privateKey
   *          The private key bytes.
   * @param data
   *          The {@link InputStream} delivering the data for which the signature should be created.
   * @return The signature bytes.
   * @throws ProcessingException
   *           If there is an error creating the signature.
   * @throws AssertionException
   *           if the private key is {@code null} or and empty array or if the {@link InputStream} is {@code null}.
   */
  byte[] createSignature(byte[] privateKey, InputStream data);

  /**
   * Verifies the given signature for the given data and public key.<br>
   * Compatible public keys can be generated using {@link #createKeyPair()}.
   *
   * @param publicKey
   *          The public key bytes.
   * @param data
   *          The {@link InputStream} providing the data to verify.
   * @param signatureToVerify
   *          The signature that should be verified against.
   * @return {@code true} if the given signature is valid for the given data and public key. {@code false} otherwise.
   * @throws ProcessingException
   *           If there is an error validating the signature.
   * @throws AssertionException
   *           If one of the arguments is {@code null} or an empty array.
   */
  boolean verifySignature(byte[] publicKey, InputStream data, byte[] signatureToVerify);

  /**
   * Creates a new key pair (private and public key).<br>
   * The result is compatible to use with {@link #createSignature(byte[], InputStream)} and
   * {@link #verifySignature(byte[], InputStream, byte[])}.
   *
   * @return The new {@link KeyPairBytes}.
   * @throws ProcessingException
   *           If there is an error generating the new keys.
   */
  KeyPairBytes createKeyPair();

  /**
   * Wraps the specified {@link InputStream} into a digest input stream.
   * <p>
   * After the specified input stream has been read, the hash can be accessed by
   * <code>DigestInputStream.getMessageDigest().digest()</code>.
   * </p>
   * <p>
   * <b>Important:</b> For hashing of passwords use {@link #createPasswordHash(char[], byte[])}!
   * </p>
   *
   * @param stream
   *          The {@link InputStream} providing the data to hash.
   * @return the digest input stream wrapping the given {@link InputStream}
   * @throws ProcessingException
   *           If there is an error creating the digest input stream
   */
  DigestInputStream toHashingStream(InputStream stream);

  /**
   * Wraps the specified {@link OutputStream} into a digest output stream.
   * <p>
   * After the specified output stream has been written, the hash can be accessed by
   * <code>DigestInputStream.getMessageDigest().digest()</code>.
   * </p>
   * <p>
   * <b>Important:</b> For hashing of passwords use {@link #createPasswordHash(char[], byte[])}!
   * </p>
   *
   * @param stream
   *          The {@link OutputStream} providing the data to hash.
   * @return the digest output stream wrapping the given {@link OutputStream}
   * @throws ProcessingException
   *           If there is an error creating the digest output stream
   */
  DigestOutputStream toHashingStream(OutputStream stream);

  /**
   * Creates a hash for the given password.<br>
   *
   * @param password
   *          The password to create the hash for. Must not be {@code null} or empty.
   * @param salt
   *          The salt to use. Use {@link #createSecureRandomBytes(int)} to generate a new random salt for each
   *          credential. Do not use the same salt for multiple credentials. The salt should be at least 32 bytes long.
   *          Remember to save the salt with the hashed password! Must not be {@code null} or an empty array.
   * @return the password hash
   * @throws AssertionException
   *           If one of the following conditions is {@code true}:<br>
   *           <ul>
   *           <li>The password is {@code null} or an empty array</li>
   *           <li>The salt is {@code null} or an empty array</li>
   *           </ul>
   * @throws ProcessingException
   *           If there is an error creating the hash. <br>
   */
  byte[] createPasswordHash(char[] password, byte[] salt);

  /**
   * This method is recommended in combination with {@link #createPasswordHash(char[], byte[])} where the iteration
   * count is omitted. This has the advantage that the check of the password hash is independent of the creation of the
   * hash. In case the iteration count is increased yearly, this method checks if the hash is valid
   *
   * @return true if calculated password hash created with {@link #createPasswordHash(char[], byte[])} matches the
   *         expected hash.
   * @since 11.0
   */
  boolean verifyPasswordHash(char[] password, byte[] salt, byte[] expectedHash);

  /**
   * Encrypts the given data using the given {@link EncryptionKey}.<br>
   * Use {@link #decrypt(InputStream, OutputStream, EncryptionKey)} to decrypt the data again using the same key.
   *
   * @param clearTextData
   *          An {@link InputStream} providing the clear text data. The {@link InputStream} is not closed by this
   *          method!
   * @param encryptedData
   *          The encrypted data is written to this {@link OutputStream}. The {@link OutputStream} will be automatically
   *          closed by this method call.
   * @param key
   *          The {@link EncryptionKey} to use.
   * @throws AssertionException
   *           If one of the parameters is {@code null}.
   * @see #createEncryptionKey(char[], byte[], int)
   * @see #decrypt(InputStream, OutputStream, EncryptionKey)
   */
  void encrypt(InputStream clearTextData, OutputStream encryptedData, EncryptionKey key);

  /**
   * Decrypts the given data using the given {@link EncryptionKey}.<br>
   * Use {@link #encrypt(InputStream, OutputStream, EncryptionKey)} to encrypt the data using the same key.
   *
   * @param encryptedData
   *          An {@link InputStream} providing the encrypted text data. The {@link InputStream} is not closed by this
   *          method!
   * @param clearTextData
   *          The clear text data is written to this {@link OutputStream}. The {@link OutputStream} will be
   *          automatically closed by this method call.
   * @param key
   *          The {@link EncryptionKey} to use.
   * @throws AssertionException
   *           If one of the parameters is {@code null}.
   * @see #createEncryptionKey(char[], byte[], int)
   * @see #encrypt(InputStream, OutputStream, EncryptionKey)
   */
  void decrypt(InputStream encryptedData, OutputStream clearTextData, EncryptionKey key);

  /**
   * Creates a new {@link EncryptionKey} to be only used with
   * {@link #encrypt(InputStream, OutputStream, EncryptionKey)}.
   * <p>
   * Warning: This key must only be used for encryption. Decryption must be backwards compatible and uses meta
   * parameters stored at beginning of stream. Therefore never use an {@link EncryptionKey} directly to decrypt but
   * always with {@link SecurityUtility#decrypt(byte[], char[], byte[], int, EncryptionKey)}
   *
   * @param password
   *          The password to use to create the key. Must not be {@code null} or empty.
   * @param salt
   *          The salt to use for the key. Must not be {@code null} or empty. It is important to create a separate
   *          random salt for each key! Salts may not be shared by several keys. Use
   *          {@link #createSecureRandomBytes(int)} to generate a new salt. It is safe to store the salt in clear text
   *          alongside the encrypted data.
   * @param keyLen
   *          The length of the key (in bits). Must be one of 128, 192 or 256.
   * @return The {@link EncryptionKey} used to encrypt data.
   * @throws AssertionException
   *           If one of the following conditions is {@code true}:<br>
   *           <ul>
   *           <li>The password is {@code null} or an empty array</li>
   *           <li>The salt is {@code null} or an empty array</li>
   *           <li>The key length is not valid.</li>
   *           </ul>
   * @see #encrypt(InputStream, OutputStream, EncryptionKey)
   */
  EncryptionKey createEncryptionKey(char[] password, byte[] salt, int keyLen);

  /**
   * Creates a backward compatible {@link EncryptionKey} that can be used in
   * {@link #decrypt(InputStream, OutputStream, EncryptionKey)}
   *
   * @param password
   *          The password to use to create the key. Must not be {@code null} or empty.
   * @param salt
   *          The salt to use for the key. Must not be {@code null} or empty. It is important to create a separate
   *          random salt for each key! Salts may not be shared by several keys. Use
   *          {@link #createSecureRandomBytes(int)} to generate a new salt. It is safe to store the salt in clear text
   *          alongside the encrypted data.
   * @param keyLen
   *          The length of the key (in bits). Must be one of 128, 192 or 256.
   * @param compatibilityHeader
   *          that was created when encrypting, see {@link EncryptionKey#getCompatibilityHeader()}
   * @return The {@link EncryptionKey} used to decrypt data.
   * @throws AssertionException
   *           If one of the following conditions is {@code true}:<br>
   *           <ul>
   *           <li>The password is {@code null} or an empty array</li>
   *           <li>The salt is {@code null} or an empty array</li>
   *           <li>The key length is not valid.</li>
   *           </ul>
   * @see #decrypt(InputStream, OutputStream, EncryptionKey)
   *      <p>
   *      Implementors of this interface should implement this method in a way that backward compatibility is
   *      guaranteed.
   */
  default EncryptionKey createDecryptionKey(char[] password, byte[] salt, int keyLen, byte[] compatibilityHeader) {
    return createEncryptionKey(password, salt, keyLen);
  }

  /**
   * Creates a new secure random instance. The returned instance has already been seeded and is ready to use.
   *
   * @return A new {@link SecureRandom} instance.
   * @see #createSecureRandomBytes(int)
   */
  SecureRandom createSecureRandom();

  /**
   * Generates a user-specified number of secure random bytes.
   *
   * @param numBytes
   *          The number of bytes to create. Must be >= 1;
   * @return the created random bytes.
   * @throws AssertionException
   *           if the size is less than 1.
   */
  byte[] createSecureRandomBytes(int numBytes);

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
  String keyStoreToHumanReadableText(InputStream keyStoreInput, String storePass, String keyPass);
}
