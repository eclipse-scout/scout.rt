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
package org.eclipse.scout.rt.server.jaxws.provider.auth.authenticator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.jaxws.JaxWsConstants;

/**
 * Authenticator to validate webservice requests against configured users in <code>config.ini</code> file. Multiple
 * credentials are separated with a semicolon, username and password with the 'equals' sign.
 * <p/>
 * Example: <code>jaxws.auth.users=jack\=XXXX;john\=XXXX;anna\=XXXX</code>
 */
public class ConfigIniAuthenticator implements IAuthenticator {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ConfigIniAuthenticator.class);
  protected static final String CONFIG_INI_EXAMPLE = String.format("%s=jack\\=XXXX;john\\=XXXX;anna\\=XXXX", JaxWsConstants.CONFIG_PROP_AUTH_USERS);

  private final Map<String, String> m_users;

  public ConfigIniAuthenticator() {
    m_users = readUsers();
  }

  @Override
  public boolean authenticate(final String username, final String passwordPlainText) throws Exception {
    return StringUtility.hasText(username) && StringUtility.hasText(passwordPlainText) && passwordPlainText.equals(m_users.get(username.toLowerCase()));
  }

  /**
   * Method invoked to read the credentials from <code>config.ini</code>.
   */
  @Internal
  protected Map<String, String> readUsers() {
    final String credentialsRaw = ConfigIniUtility.getProperty(JaxWsConstants.CONFIG_PROP_AUTH_USERS);
    if (credentialsRaw == null) {
      return Collections.emptyMap();
    }

    final Map<String, String> credentialMap = new HashMap<>();

    for (final String credentialRaw : credentialsRaw.split(";")) {
      final String[] credential = credentialRaw.split("=", 2);
      if (credential.length == 2) {
        final String username = credential[0];
        if (!StringUtility.hasText(username)) {
          LOG.warn("Configured username must not be empty. [example={}]", CONFIG_INI_EXAMPLE);
          continue;
        }

        final String password = credential[1];
        if (!StringUtility.hasText(password)) {
          LOG.warn("Configured password must not be empty. [example={}]", CONFIG_INI_EXAMPLE);
          continue;
        }

        credentialMap.put(username.toLowerCase(), password);
      }
      else {
        LOG.warn("Username and password must be separated with the 'equals' sign.  [example={}]", CONFIG_INI_EXAMPLE);
      }
    }

    return credentialMap;
  }
}
