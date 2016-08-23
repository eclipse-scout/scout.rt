package org.eclipse.scout.rt.mom.api.encrypter;

import java.security.GeneralSecurityException;
import java.util.Map;

import org.eclipse.scout.rt.mom.api.IMom;

/**
 * Provides 'end-to-end' encryption for messages and properties sent through {@link IMom}.
 *
 * @since 6.1
 * @see IMom
 */
public interface IEncrypter {

  /**
   * Provides a new encryption context initialized with data like initial vector for symmetric encryption to encrypt and
   * decrypt a message, and is invoked immediately before the encryption of a message and its properties starts.
   * <p>
   * The context is transmitted to the recipient in clear text.
   */
  Map<String, String> newContext();

  /**
   * Encrypts the given message. The encryption context contains data as provided by {@link #newContext()}, and is
   * allowed to be modified during encryption, e.g. to provide a signature in asymmetric encryption.
   * <p>
   * The context is transmitted to the recipient in clear text.
   */
  byte[] encrypt(byte[] clearText, Map<String, String> context) throws GeneralSecurityException;

  /**
   * Decrypts the given message. The encryption context contains data set during encryption.
   */
  byte[] decrypt(byte[] encryptedData, Map<String, String> context) throws GeneralSecurityException;

  /**
   * Encrypts the given property. The encryption context contains data as provided by {@link #newContext()}, and is
   * allowed to be modified during encryption, e.g. to provide a signature in asymmetric encryption.
   * <p>
   * The context is transmitted to the recipient in clear text.
   */
  byte[] encryptProperty(String property, byte[] clearText, Map<String, String> context) throws GeneralSecurityException;

  /**
   * Decrypts the given property. The encryption context contains data set during encryption.
   */
  byte[] decryptProperty(String property, byte[] encryptedData, Map<String, String> context) throws GeneralSecurityException;
}
