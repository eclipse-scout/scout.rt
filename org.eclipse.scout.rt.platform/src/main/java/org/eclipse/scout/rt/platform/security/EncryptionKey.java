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

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;

/**
 * Class describing a symmetric encryption key.
 */
public class EncryptionKey {
  private final Key m_key;
  private final AlgorithmParameterSpec m_params;

  public EncryptionKey(Key key, AlgorithmParameterSpec params) {
    m_key = key;
    m_params = params;
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
}
