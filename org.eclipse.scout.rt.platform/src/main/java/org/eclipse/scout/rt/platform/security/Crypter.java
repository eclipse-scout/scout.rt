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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * This class provides support for symmetric encryption/decryption. {@link StandardCharsets#UTF_8} is used.
 * <p>
 * Call {@link #init(char[], int)} before using this class to encrypt/decrypt.
 * <p>
 * The result created by {@link #encrypt(String)} or {@link #encryptUrlSafe(String)} consists of three part, each
 * separated by a dot.
 * <ul>
 * <li>Part 0: profile
 * <li>Part 1: salt
 * <li>Part 2: encrypted data
 * </ul>
 * The salt and encrypted data are base64-encoded (optionally url safe). The profile indicator is used to distinguish
 * between different supported encryption profiles. Currently, there is only one supported profile, the one used within
 * {@link ISecurityProvider}. If in a future release the default implementation within {@link ISecurityProvider}
 * changes, a new profile will be added to support the decryption of legacy secrets.
 */
@Bean
public class Crypter {

  /**
   * The profile must never contain a dot.
   * <p>
   * The current profile uses the default from {@link ISecurityProvider} for encrypting/decrypting data.
   */
  protected static final String CURRENT_PROFILE = "0";

  private char[] m_password;
  private int m_keyLength;

  /**
   * Initializes the crypter with password and encryption key length.
   */
  public Crypter init(char[] password, int keyLength) {
    Assertions.assertNotNull(password, "Password must not be null");
    Assertions.assertNull(m_password, "Initialization can only be called once without prior reset");
    m_password = password;
    m_keyLength = keyLength;
    return this;
  }

  /**
   * Resets the crypter by clearing the password.
   */
  public void reset() {
    if (m_password == null) {
      return;
    }
    Arrays.fill(m_password, (char) 0);
    m_password = null;
  }

  protected char[] getPassword() {
    return m_password;
  }

  protected int getKeyLength() {
    return m_keyLength;
  }

  /**
   * Encrypts the clear text data.
   *
   * @return See {@link Crypter} for definition of format.
   */
  public String encrypt(String clearTextData) {
    return encrypt(clearTextData, false);
  }

  /**
   * Encrypts the clear text data.
   *
   * @return See {@link Crypter} for definition of format.
   */
  public String encryptUrlSafe(String clearTextData) {
    return encrypt(clearTextData, true);
  }

  protected String encrypt(String clearTextData, boolean urlSafe) {
    Assertions.assertNotNull(getPassword(), "Crypter was not initialized");
    if (StringUtility.isNullOrEmpty(clearTextData)) {
      return null;
    }

    byte[] salt = SecurityUtility.createRandomBytes();
    EncryptionKey encryptionKey = SecurityUtility.createEncryptionKey(getPassword(), salt, getKeyLength());

    byte[] encryption = SecurityUtility.encrypt(clearTextData.getBytes(StandardCharsets.UTF_8), encryptionKey);
    return StringUtility.join(".", CURRENT_PROFILE, Base64Utility.encode(salt, urlSafe), Base64Utility.encode(encryption, urlSafe));
  }

  /**
   * Decrypts the encrypted data.
   *
   * @param encryptedData
   *     Encrypted data as returned by {@link #encrypt(String)}.
   */
  public String decrypt(String encryptedData) {
    return decrypt(encryptedData, false);
  }

  /**
   * Decrypts the encrypted data.
   *
   * @param encryptedData
   *     Encrypted data as returned by {@link #encryptUrlSafe(String)}.
   */
  public String decryptUrlSafe(String encryptedData) {
    return decrypt(encryptedData, true);
  }

  protected String decrypt(String encryptedData, boolean urlSafe) {
    Assertions.assertNotNull(getPassword(), "Crypter was not initialized");
    if (StringUtility.isNullOrEmpty(encryptedData)) {
      return null;
    }

    String[] encryptedDataParts = encryptedData.split("\\.", 3);
    if (encryptedDataParts.length != 3) {
      throw new ProcessingException("Format of encrypted data is invalid (part separation missing)");
    }

    if (StringUtility.isNullOrEmpty(encryptedDataParts[0])
        || StringUtility.isNullOrEmpty(encryptedDataParts[1])
        || StringUtility.isNullOrEmpty(encryptedDataParts[2])) {
      throw new ProcessingException("Format of encrypted data is invalid (empty parts)");
    }

    String profile = encryptedDataParts[0];
    byte[] salt = Base64Utility.decode(encryptedDataParts[1], urlSafe);
    byte[] encrypted = Base64Utility.decode(encryptedDataParts[2], urlSafe);

    // Currently only one profile exists, there might be others in future release (e.g. other encryption algorithms, ...)
    if (!CURRENT_PROFILE.equals(profile)) {
      throw new ProcessingException("Encrypted data uses unsupported profile '{}'", profile);
    }

    byte[] decrypted = SecurityUtility.decrypt(encrypted, getPassword(), salt, getKeyLength());
    return new String(decrypted, StandardCharsets.UTF_8);
  }
}
