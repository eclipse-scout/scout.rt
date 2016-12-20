package org.eclipse.scout.rt.platform.security;

import java.util.Arrays;

/**
 * Public and private key bytes.
 */
public class KeyPairBytes {

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
