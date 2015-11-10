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

import javax.annotation.PostConstruct;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Credential verifier against <i>config.properties</i> file.
 * <p>
 * Credentials are loaded from property {@link CredentialsProperty}. Multiple credentials are separated with a
 * semicolon, username and password with the colon.
 * <p/>
 * Example: <code>scott:XXXX;jack:XXXX;john:XXXX</code>
 */
@Bean
public class ConfigFileCredentialVerifier implements ICredentialVerifier {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigFileCredentialVerifier.class);

  private final Map<String, char[]> m_credentials;

  public ConfigFileCredentialVerifier() {
    m_credentials = new HashMap<>();
  }

  /**
   * Overwrite to initialize this verifier with credentials.
   * <p>
   * The default implementation loads credentials from {@link CredentialsProperty}.
   */
  @PostConstruct
  protected void init() {
    loadCredentials(BEANS.get(CredentialsProperty.class));
  }

  /**
   * Invoke to load credentials represented by the given config-property.
   */
  protected final void loadCredentials(final IConfigProperty<String> configProperty) {
    m_credentials.clear();

    final String credentialsRaw = configProperty.getValue();
    if (credentialsRaw == null) {
      return;
    }

    final String credentialSample = String.format("%s=scott:XXXX;jack:XXXX;john:XXXX", configProperty.getKey());

    for (final String credentialRaw : credentialsRaw.split(";")) {
      final String[] userPass = credentialRaw.split(":", 2);
      if (userPass.length == 2) {
        final String username = userPass[0];
        if (!StringUtility.hasText(username)) {
          LOG.warn("Configured username must not be empty. [example={}]", credentialSample);
          continue;
        }

        final String password = userPass[1];
        if (!StringUtility.hasText(password)) {
          LOG.warn("Configured password must not be empty. [example={}]", credentialSample);
          continue;
        }

        m_credentials.put(username.toLowerCase(), password.toCharArray());
      }
      else {
        LOG.warn("Username and password must be separated with the 'colon' sign.  [example={}]", credentialSample);
      }
    }
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

  /**
   * Represents credentials to be loaded into {@link ConfigFileCredentialVerifier}.
   * <p>
   * Multiple credentials are separated with a semicolon, username and password with the 'colon' sign.
   * <p/>
   * Example: <code>scott:XXXX;jack:XXXX;john:XXXX</code>
   */
  public static class CredentialsProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.auth.credentials";
    }
  }
}
