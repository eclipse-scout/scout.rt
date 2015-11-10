/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.authentication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.platform.Bean;

/**
 * Use this class to verify credentials against credentials registered to this In-Memory verifier.
 *
 * @see #addCredential(String, char[])
 * @since 5.2
 */
@Bean
public class InMemoryCredentialVerifier implements ICredentialVerifier {

  private final Map<String, char[]> m_credentials;

  public InMemoryCredentialVerifier() {
    m_credentials = new HashMap<>();
  }

  /**
   * Adds the given credentials to this In-Memory user store.
   */
  public void addCredential(final String username, final char[] password) {
    m_credentials.put(username, password);
  }

  @Override
  public int verify(final String username, final char[] password) {
    if (StringUtility.isNullOrEmpty(username)) {
      return AUTH_CREDENTIALS_REQUIRED;
    }

    if (password == null || password.length == 0) {
      return AUTH_CREDENTIALS_REQUIRED;
    }

    if (!Arrays.equals(password, m_credentials.get(username.toLowerCase()))) {
      return AUTH_FORBIDDEN;
    }

    return AUTH_OK;
  }
}
