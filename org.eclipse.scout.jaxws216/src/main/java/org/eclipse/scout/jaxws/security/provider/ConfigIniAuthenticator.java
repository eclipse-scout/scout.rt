/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.security.provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;

/**
 * Authenticator to validate a user's credential against configured users in config.ini file.
 * <p/>
 * <strong>config-ini-property:</strong><br/>
 * <code>org.eclipse.scout.jaxws.security.provider.ConfigIniAuthenticator#credentials</code>
 * <p/>
 * Multiple credentials are separated with a semicolon, username and password with the 'equals' sign, e.g.
 * <code>org.eclipse.scout.jaxws.security.provider.ConfigIniAuthenticator#credentials=jack\=XXXX;john\=XXXX;anna\=XXXX</code>
 */
@ApplicationScoped
public class ConfigIniAuthenticator implements IAuthenticator {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ConfigIniAuthenticator.class);

  private static final String PROP_CREDENTIALS = String.format("%s#credentials", ConfigIniAuthenticator.class.getName());
  private static final String CONFIG_INI_EXAMPLE = String.format("%s=jack\\=XXXX;john\\=XXXX;anna\\=XXXX", PROP_CREDENTIALS);

  private final Map<String, String> m_credentials;

  public ConfigIniAuthenticator() {
    m_credentials = readCredentialsFromConfigIni();
  }

  @Override
  public boolean authenticate(final String username, final String passwordPlainText) throws Exception {
    return StringUtility.hasText(username) && StringUtility.hasText(passwordPlainText) && passwordPlainText.equals(m_credentials.get(username.toLowerCase()));
  }

  /**
   * Method invoked to read the credentials from <code>config.ini</code>.
   */
  @Internal
  protected Map<String, String> readCredentialsFromConfigIni() {
    final String credentialsRaw = ConfigIniUtility.getProperty(PROP_CREDENTIALS);
    if (credentialsRaw == null) {
      return Collections.emptyMap();
    }

    final Map<String, String> credentialMap = new HashMap<>();

    for (final String credentialRaw : credentialsRaw.split(";")) {
      final String[] credential = credentialRaw.split("=", 2);
      if (credential.length == 2) {
        final String username = credential[0];
        if (!StringUtility.hasText(username)) {
          LOG.warn("Configured username must not be empty. {}", getExampleConfig());
          continue;
        }

        final String password = credential[1];
        if (!StringUtility.hasText(password)) {
          LOG.warn("Configured password must not be empty. {}", getExampleConfig());
          continue;
        }

        credentialMap.put(username.toLowerCase(), password);
      }
      else {
        LOG.warn("username and password must be separated with the 'equals' sign. {}", getExampleConfig());
      }
    }

    return credentialMap;
  }

  private String getExampleConfig() {
    return String.format("Please see the example config: %s", CONFIG_INI_EXAMPLE);
  }
}
