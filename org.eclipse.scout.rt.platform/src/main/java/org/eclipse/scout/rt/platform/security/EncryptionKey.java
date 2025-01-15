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

import java.io.PushbackInputStream;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;

/**
 * Class describing a symmetric encryption key.
 */
public class EncryptionKey {
  private final Key m_key;
  private final AlgorithmParameterSpec m_params;
  private final byte[] m_compatibilityHeader;

  /**
   * @deprecated use {@link #EncryptionKey(Key, AlgorithmParameterSpec, byte[])} instead
   */
  @Deprecated
  public EncryptionKey(Key key, AlgorithmParameterSpec params) {
    this(key, params, null);
  }

  public EncryptionKey(Key key, AlgorithmParameterSpec params, byte[] compatibilityHeader) {
    m_key = key;
    m_params = params;
    m_compatibilityHeader = compatibilityHeader;
  }

  /**
   * @return The {@link Key} instance.
   */
  public Key get() {
    return m_key;
  }

  /**
   * @return The {@link AlgorithmParameterSpec} needed to initialize a {@link Cipher} with this key.
   */
  public AlgorithmParameterSpec params() {
    return m_params;
  }

  /**
   * @return a byte array that can be used by implementors to support backwards compatibility. This header may be
   * prefixed to the encrypted stream.
   * <p>
   * The format of such a header is <code>[yyyy:version]</code> encoded in US ASCII. yyyy is the 4-digit year
   * and version is * freetext without the ']' character. Example: [2023:v1]
   * <p>
   * See {@link SecurityUtility#createEncryptionKey(char[], byte[], int)} and
   * {@link SecurityUtility#createDecryptionKey(PushbackInputStream, char[], byte[], int, EncryptionKey)}
   */
  public byte[] getCompatibilityHeader() {
    return m_compatibilityHeader;
  }
}
