package org.eclipse.scout.rt.platform.security;

import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Arrays;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

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
   *          The password to create the authentication code.
   * @param data
   *          The {@link InputStream} that provides the data for which the MAC should be created.
   * @return The created authentication code.
   * @throws ProcessingException
   *           if there is an error creating the MAC
   * @throws IllegalArgumentException
   *           if the password or data is <code>null</code>.
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
   * @throws IllegalArgumentException
   *           if the private key or {@link InputStream} is <code>null</code>.
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
   * @return <code>true</code> if the given signature is valid for the given data and public key. <code>false</code>
   *         otherwise.
   * @throws ProcessingException
   *           If there is an error validating the signature.
   * @throws IllegalArgumentException
   *           If one of the arguments is <code>null</code>.
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
   * <b>Important:</b> For hashing of passwords use {@link #createPasswordHash(String, byte[], int)}!
   *
   * @param data
   *          The {@link InputStream} providing the data to hash.
   * @param salt
   *          the salt to use. Use {@link #createSecureRandomBytes(int)} to generate a random salt per instance.
   * @return the hash
   * @throws IllegalArgumentException
   *           If data is <code>null</code>.
   * @throws ProcessingException
   *           If there is an error creating the hash
   */
  byte[] createHash(InputStream data, byte[] salt);

  /**
   * Creates a hash for the given password.<br>
   *
   * @param password
   *          The password to create the hash for. Must not be <code>null</code>.
   * @param salt
   *          The salt to use. Use {@link #createSecureRandomBytes(int)} to generate a new random salt for each
   *          credential. Do not use the same salt for multiple credentials. The salt should be at least 32 bytes long.
   *          Remember to save the salt with the hashed password! Must not be <code>null</code> or an empty array.
   * @param iterations
   *          Specifies how many times the method executes its underlying algorithm. A higher value is safer.<br>
   *          While there is a minimum number of iterations recommended to ensure data safety, this value changes every
   *          year as technology improves. As by May 2016 at least 10000 iterations are recommended. Therefore this
   *          method will not accept values below that limit.<br>
   *          Experimentation is important. To provide a good security use an iteration count so that the call to this
   *          method requires one half second to execute (on the production system). Also consider the number of users
   *          and the number of logins executed to find a value that scales in your environment.
   * @return the password hash
   * @throws IllegalArgumentException
   *           If the password is <code>null</code>, the salt is <code>null</code> or empty or iterations is not
   *           positive.
   * @throws ProcessingException
   *           If there is an error creating the hash. <br>
   */
  byte[] createPasswordHash(String password, byte[] salt, int iterations);

  /**
   * Encrypts the given data using the given key and salt.<br>
   * Use {@link #decrypt(byte[], String, byte[], int)} to decrypt the data again (using the same key, salt and keyLen).
   *
   * @param clearTextData
   *          The data to encrypt.
   * @param password
   *          The password to use for the encryption. Must not be <code>null</code> or empty.
   * @param salt
   *          The salt to use for the encryption. Must not be <code>null</code> or empty. It is important to create a
   *          separate random salt for each key! Salts may not be shared by several keys. Use
   *          {@link #createSecureRandomBytes(int)} to generate a new salt for a key. It is safe to store the salt in
   *          clear text alongside the encrypted data. This salt will then be used to decrypt the data again.
   * @param keyLen
   *          The key length (in bits) to use.
   * @return The encrypted data.
   * @throws ProcessingException
   *           If there is an error during encryption.
   * @throws IllegalArgumentException
   *           If the clear text data, salt or password is <code>null</code> or empty or an unsupported keyLen has been
   *           provided.
   */
  byte[] encrypt(byte[] clearTextData, String password, byte[] salt, int keyLen);

  /**
   * Decrypts the data using the given key and salt.<br>
   * This method is intended to decrypt data that was previously encrypted using
   * {@link #encrypt(byte[], String, byte[], int)}.<br>
   *
   * @param encryptedData
   *          The encrypted data that should be decrypted.
   * @param password
   *          The password to use for the decryption. Must not be <code>null</code> or empty.
   * @param salt
   *          The salt to use for the decryption. This is the same salt that was used to encrypt the data.
   * @param keyLen
   *          The key length (in bits) to use.
   * @return The original clear text data.
   * @throws ProcessingException
   *           If there is an error during decryption.
   * @throws IllegalArgumentException
   *           If the encrypted data, salt or password is <code>null</code> or empty or an unsupported keyLen has been
   *           provided.
   */
  byte[] decrypt(byte[] encryptedData, String password, byte[] salt, int keyLen);

  /**
   * Creates a new secure random instance. The returned instance has already been seeded and is ready to use.
   *
   * @return A new self-seeded {@link SecureRandom} instance.
   * @see #createSecureRandomBytes(int)
   */
  SecureRandom createSecureRandom();

  /**
   * Generates a user-specified number of random bytes.
   *
   * @param numBytes
   *          The number of bytes to create.
   * @return the created random bytes.
   * @throws IllegalArgumentException
   *           if the size is less than 1.
   */
  byte[] createSecureRandomBytes(int numBytes);

  /**
   * Public and private key bytes.
   */
  public final class KeyPairBytes {

    private final byte[] m_privateKey;
    private final byte[] m_publicKey;

    public KeyPairBytes(byte[] priv, byte[] pub) {
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
      if (getClass() != obj.getClass()) {
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
