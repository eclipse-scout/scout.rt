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
import java.io.InputStream;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.security.ISecurityProvider.KeyPairBytes;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.HexUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Utility class for encryption & decryption, hashing and creation of random numbers, message authentication codes and
 * digital signatures.<br>
 * The {@link Base64Utility} or {@link HexUtility} can be used to encode the bytes returned by this class.
 *
 * @since 5.1
 * @see Base64Utility
 * @see HexUtility
 */
public final class SecurityUtility {

  /**
   * Specifies the default iteration count for hashing.
   */
  private static final int DEFAULT_HASHING_CYCLES = 3557;

  /**
   * Number of random bytes to be created by default.
   */
  private static final int DEFAULT_RANDOM_SIZE = 16;

  private SecurityUtility() {
    // no instances of this class
  }

  /**
   * See {@link ISecurityProvider#decrypt(byte[], String, byte[], int)}
   */
  public static byte[] decrypt(byte[] encryptedData, String password, byte[] salt, int keyLen) {
    return BEANS.get(ISecurityProvider.class).decrypt(encryptedData, password, salt, keyLen);
  }

  /**
   * See {@link ISecurityProvider#encrypt(byte[], String, byte[], int)}
   */
  public static byte[] encrypt(byte[] clearTextData, String password, byte[] salt, int keyLen) {
    return BEANS.get(ISecurityProvider.class).encrypt(clearTextData, password, salt, keyLen);
  }

  /**
   * See {@link ISecurityProvider#createSecureRandomBytes(int)}
   */
  public static byte[] createRandomBytes(int numBytes) {
    return BEANS.get(ISecurityProvider.class).createSecureRandomBytes(numBytes);
  }

  /**
   * Generates 16 random bytes.
   *
   * @return the created random bytes.
   * @throws ProcessingException
   *           If the current platform does not support the random number generation algorithm.
   * @see ISecurityProvider#createRandomBytes(int)
   */
  public static byte[] createRandomBytes() {
    return createRandomBytes(DEFAULT_RANDOM_SIZE);
  }

  /**
   * See {@link ISecurityProvider#createSecureRandom()}
   */
  public static SecureRandom createSecureRandom() {
    return BEANS.get(ISecurityProvider.class).createSecureRandom();
  }

  /**
   * Creates a hash using the given data input and salt.
   *
   * @param data
   *          the data to hash
   * @param salt
   *          the salt to use. Use {@link #createRandomBytes()} to generate a random salt per instance.
   * @return the hash
   * @throws ProcessingException
   *           If there is an error creating the hash
   * @throws IllegalArgumentException
   *           If data is null.
   */
  public static byte[] hash(byte[] data, byte[] salt) {
    return hash(data, salt, DEFAULT_HASHING_CYCLES);
  }

  /**
   * Creates a hash using the given data input and salt.
   *
   * @param data
   *          the data to hash
   * @param salt
   *          the salt to use. Use {@link #createRandomBytes()} to generate a random salt per instance.
   * @param iterations
   *          the number of cycles to hash. There is always at least one cycle executed.
   * @return the hash
   * @throws ProcessingException
   *           If there is an error creating the hash
   * @throws IllegalArgumentException
   *           If data is null.
   */
  public static byte[] hash(byte[] data, byte[] salt, int iterations) {
    if (data == null) {
      throw new IllegalArgumentException("no data provided");
    }
    return hash(new ByteArrayInputStream(data), salt, iterations);
  }

  /**
   * See {@link ISecurityProvider#createHash(InputStream, byte[], int)}
   */
  public static byte[] hash(InputStream data, byte[] salt, int iterations) {
    return BEANS.get(ISecurityProvider.class).createHash(data, salt, iterations);
  }

  /**
   * See {@link ISecurityProvider#createKeyPair()}
   */
  public static KeyPairBytes generateKeyPair() {
    return BEANS.get(ISecurityProvider.class).createKeyPair();
  }

  /**
   * See {@link ISecurityProvider#createSignature(byte[], InputStream)}
   */
  public static byte[] createSignature(byte[] privateKey, InputStream data) {
    return BEANS.get(ISecurityProvider.class).createSignature(privateKey, data);
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
   * @throws IllegalArgumentException
   *           if the private key or data is <code>null</code>.
   */
  public static byte[] createSignature(byte[] privateKey, byte[] data) {
    if (data == null) {
      throw new IllegalArgumentException("no data provided");
    }
    return createSignature(privateKey, new ByteArrayInputStream(data));
  }

  /**
   * See {@link ISecurityProvider#verifySignature(byte[], InputStream, byte[])}
   */
  public static boolean verifySignature(byte[] publicKey, InputStream data, byte[] signatureToVerify) {
    return BEANS.get(ISecurityProvider.class).verifySignature(publicKey, data, signatureToVerify);
  }

  /**
   * Verifies the given signature for the given data and public key.<br>
   * Compatible public keys can be generated using {@link #generateKeyPair()}.
   *
   * @param publicKey
   *          The public key bytes.
   * @param data
   *          The data for which the signature should be validated.
   * @param signatureToVerify
   *          The signature that should be verified against.
   * @return <code>true</code> if the given signature is valid for the given data and public key. <code>false</code>
   *         otherwise.
   * @throws ProcessingException
   *           If there is an error validating the signature.
   * @throws IllegalArgumentException
   *           if one of the arguments is <code>null</code>.
   */
  public static boolean verifySignature(byte[] publicKey, byte[] data, byte[] signatureToVerify) {
    if (data == null) {
      throw new IllegalArgumentException("no data provided");
    }
    return verifySignature(publicKey, new ByteArrayInputStream(data), signatureToVerify);
  }

  /**
   * See {@link ISecurityProvider#createMac(byte[], InputStream)}
   */
  public static byte[] createMac(byte[] password, InputStream data) {
    return BEANS.get(ISecurityProvider.class).createMac(password, data);
  }

  /**
   * Create a Message Authentication Code (MAC) for the given data and password.
   *
   * @param password
   *          The password to create the authentication code.
   * @param data
   *          The data for which the code should be created.
   * @return The created authentication code.
   * @throws ProcessingException
   *           if there is an error creating the MAC
   * @throws IllegalArgumentException
   *           if the password or data is <code>null</code>.
   */
  public static byte[] createMac(byte[] password, byte[] data) {
    if (data == null) {
      throw new IllegalArgumentException("no data provided");
    }
    return createMac(password, new ByteArrayInputStream(data));
  }

  /**
   * @return the principal names of the given {@link Subject}, or <code>null</code> if the given {@link Subject} is
   *         <code>null</code>. Multiple principal names are separated by comma.
   */
  public static String getPrincipalNames(Subject subject) {
    if (subject == null) {
      return null;
    }

    final List<String> principalNames = new ArrayList<>(subject.getPrincipals().size());
    for (final Principal principal : subject.getPrincipals()) {
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
