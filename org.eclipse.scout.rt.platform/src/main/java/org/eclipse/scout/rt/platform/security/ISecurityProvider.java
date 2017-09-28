/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.security;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * <h3>{@link ISecurityProvider}</h3> Provider class for encryption & decryption, hashing and creation of random
 * numbers, message authentication codes and digital signatures.<br>
 *
 * @since 6.1
 */
@ApplicationScoped
public interface ISecurityProvider {

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
   * Creates a hash for the given data using the given salt.<br>
   * <br>
   * <b>Important:</b> For hashing of passwords use {@link #createPasswordHash(char[], byte[], int)}!
   *
   * @param data
   *          The {@link InputStream} providing the data to hash.
   * @param salt
   *          the salt to use or {@code null} if not salt should be used (not recommended!). Use
   *          {@link #createSecureRandomBytes(int)} to generate a random salt per instance.
   * @param iterations
   *          the number of hashing iterations. There is always at least one cycle executed.
   * @return the hash
   * @throws AssertionException
   *           If data is {@code null}.
   * @throws ProcessingException
   *           If there is an error creating the hash
   */
  byte[] createHash(InputStream data, byte[] salt, int iterations);

  /**
   * Creates a hash for the given password.<br>
   *
   * @param password
   *          The password to create the hash for. Must not be {@code null} or empty.
   * @param salt
   *          The salt to use. Use {@link #createSecureRandomBytes(int)} to generate a new random salt for each
   *          credential. Do not use the same salt for multiple credentials. The salt should be at least 32 bytes long.
   *          Remember to save the salt with the hashed password! Must not be {@code null} or an empty array.
   * @param iterations
   *          Specifies how many times the method executes its underlying algorithm. A higher value is safer.<br>
   *          While there is a minimum number of iterations recommended to ensure data safety, this value changes every
   *          year as technology improves. As by May 2016 at least 10000 iterations are recommended. Therefore this
   *          method will not accept values below that limit.<br>
   *          Experimentation is important. To provide a good security use an iteration count so that the call to this
   *          method requires one half second to execute (on the production system). Also consider the number of users
   *          and the number of logins executed to find a value that scales in your environment.
   * @return the password hash
   * @throws AssertionException
   *           If one of the following conditions is {@code true}:<br>
   *           <ul>
   *           <li>The password is {@code null} or an empty array</li>
   *           <li>The salt is {@code null} or an empty array</li>
   *           <li>The number of iterations is too small.</li>
   *           </ul>
   * @throws ProcessingException
   *           If there is an error creating the hash. <br>
   */
  byte[] createPasswordHash(char[] password, byte[] salt, int iterations);

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
   * Creates a new {@link EncryptionKey} to be used with {@link #encrypt(InputStream, OutputStream, EncryptionKey)} or
   * {@link #decrypt(InputStream, OutputStream, EncryptionKey)}.
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
   * @return The {@link EncryptionKey} used to encrypt or decrypt data.
   * @throws AssertionException
   *           If one of the following conditions is {@code true}:<br>
   *           <ul>
   *           <li>The password is {@code null} or an empty array</li>
   *           <li>The salt is {@code null} or an empty array</li>
   *           <li>The key length is not valid.</li>
   *           </ul>
   * @see #encrypt(InputStream, OutputStream, EncryptionKey)
   * @see #decrypt(InputStream, OutputStream, EncryptionKey)
   */
  EncryptionKey createEncryptionKey(char[] password, byte[] salt, int keyLen);

  /**
   * Creates a new secure random instance. The returned instance has already been seeded and is ready to use.
   *
   * @return A new self-seeded {@link SecureRandom} instance.
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
}
