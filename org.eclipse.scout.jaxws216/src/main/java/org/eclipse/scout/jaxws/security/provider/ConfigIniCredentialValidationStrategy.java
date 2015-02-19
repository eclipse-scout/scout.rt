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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.ServiceUtility;

/**
 * <p>
 * Strategy to validate a user's credential against configured users in config.ini file.
 * </p>
 * <p>
 * Valid users are to be configured with the property
 * <code>org.eclipse.scout.jaxws.security.provider.ConfigIniCredentialValidationStrategy#credentials</code> in
 * config.ini.
 * </p>
 * <p>
 * Multiple credentials are separated by a semicolon, username and password by the equals sign.<br/>
 * e.g.
 * <code>org.eclipse.scout.jaxws.security.provider.ConfigIniCredentialValidationStrategy#credentials=sean\=XXXX;jack\=XXXX;kimberley\=XXXX</code>
 * </p>
 */
public class ConfigIniCredentialValidationStrategy implements ICredentialValidationStrategy {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ConfigIniCredentialValidationStrategy.class);

  private String m_credentials;
  private Map<String, String> m_credentialsMap;

  public ConfigIniCredentialValidationStrategy() {
    m_credentialsMap = new HashMap<String, String>();
    ServiceUtility.injectConfigProperties(this);
    init();
  }

  @Override
  public boolean isValidUser(String username, String passwordPlainText) throws Exception {
    if (!StringUtility.hasText(username) || !StringUtility.hasText(passwordPlainText)) {
      return false;
    }

    if (m_credentialsMap.containsKey(username.toLowerCase())) {
      return CompareUtility.equals(m_credentialsMap.get(username.toLowerCase()), passwordPlainText);
    }
    return false;
  }

  protected void init() {
    if (!StringUtility.hasText(m_credentials) || m_credentials.split(";").length == 0) {
      LOG.info("No user's credentials configured. " + getExampleConfig());
      return;
    }

    String[] credentialList = m_credentials.split(";");

    for (String credentials : credentialList) {
      String[] userpass = credentials.split("=", 2);
      if (userpass.length == 2) {
        String username = userpass[0];
        if (!StringUtility.hasText(username)) {
          LOG.warn("Configured username must not be empty. " + getExampleConfig());
          continue;
        }

        String password = userpass[1];
        if (!StringUtility.hasText(password)) {
          LOG.warn("Configured password must not be empty. " + getExampleConfig());
          continue;
        }
        m_credentialsMap.put(username.toLowerCase(), password);
      }
      else {
        LOG.warn("username and password must be separated by the equals sign. " + getExampleConfig());
      }
    }
  }

  public void setCredentials(String credentials) {
    m_credentials = credentials;
  }

  private String getExampleConfig() {
    return "Please see the exmaple config: " + ConfigIniCredentialValidationStrategy.class.getName() + "#credentials=sean\\=XXXX;jack\\=XXXX;kimberley\\=XXXX";
  }
}
