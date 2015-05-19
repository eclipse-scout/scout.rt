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

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsAuthUsersProperty;

/**
 * Authenticator to validate webservice requests against configured users in <code>config.properties</code> file.
 * Multiple credentials are separated with a semicolon, username and password with the 'equals' sign.
 * <p/>
 * Example: <code>jaxws.auth.users=anna\=XXXX;jack\=XXXX;john\=XXXX</code>
 */
public class ConfigFileAuthenticator implements IAuthenticator {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ConfigFileAuthenticator.class);

  private final Map<String, String> m_users;

  public ConfigFileAuthenticator() {
    m_users = readUsers();
  }

  @Override
  public boolean authenticate(final String username, final String passwordPlainText) throws Exception {
    return StringUtility.hasText(username) && StringUtility.hasText(passwordPlainText) && passwordPlainText.equals(m_users.get(username.toLowerCase()));
  }

  /**
   * Method invoked to read the credentials from <code>config.properties</code>.
   */
  @Internal
  protected Map<String, String> readUsers() {
    IConfigProperty<String> usersProperty = CONFIG.getProperty(JaxWsAuthUsersProperty.class);
    final String credentialsRaw = usersProperty.getValue();
    if (credentialsRaw == null) {
      return Collections.emptyMap();
    }

    final Map<String, String> credentialMap = new HashMap<>();

    for (final String credentialRaw : credentialsRaw.split(";")) {
      final String[] credential = credentialRaw.split("=", 2);
      final String configSample = String.format("%s=jack\\=XXXX;john\\=XXXX;anna\\=XXXX", usersProperty.getKey());
      if (credential.length == 2) {
        final String username = credential[0];
        if (!StringUtility.hasText(username)) {
          LOG.warn("Configured username must not be empty. [example={}]", configSample);
          continue;
        }

        final String password = credential[1];
        if (!StringUtility.hasText(password)) {
          LOG.warn("Configured password must not be empty. [example={}]", configSample);
          continue;
        }

        credentialMap.put(username.toLowerCase(), password);
      }
      else {
        LOG.warn("Username and password must be separated with the 'equals' sign.  [example={}]", configSample);
      }
    }

    return credentialMap;
  }
}
