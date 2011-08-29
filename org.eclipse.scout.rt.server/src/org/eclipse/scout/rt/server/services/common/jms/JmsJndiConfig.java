/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.jms;

import java.util.Hashtable;
import java.util.zip.CRC32;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.ProcessingException;

public class JmsJndiConfig {

  // JNDI config
  // optional, if in same context as application itself
  private String m_initialContextFactory; // "org.jnp.interfaces.NamingContextFactory"
  private String m_providerUrl; // "localhost:1099"
  private String m_userName;
  private String m_password;
  // required
  private String m_connectionFactoryJndiName; // "ConnectionFactory"
  private String m_jndiName; // "queue/A" OR "topic/testTopic"

  public String getInitialContextFactory() {
    return m_initialContextFactory;
  }

  public void setInitialContextFactory(String contextFactory) {
    m_initialContextFactory = contextFactory;
  }

  public String getProviderUrl() {
    return m_providerUrl;
  }

  public void setProviderUrl(String url) {
    m_providerUrl = url;
  }

  public String getUserName() {
    return m_userName;
  }

  public void setUserName(String name) {
    m_userName = name;
  }

  public String getPassword() {
    return m_password;
  }

  public void setPassword(String password) {
    m_password = password;
  }

  public String getConnectionFactoryJndiName() {
    return m_connectionFactoryJndiName;
  }

  public void setConnectionFactoryJndiName(String factoryJndiName) {
    m_connectionFactoryJndiName = factoryJndiName;
  }

  public String getJndiName() {
    return m_jndiName;
  }

  public void setJndiName(String name) {
    m_jndiName = name;
  }

  /**
   * Creates the initial JNDI context to work with.
   * 
   * @return context {@link InitialContext}
   * @throws ProcessingException
   */
  public InitialContext createInitialContext() throws ProcessingException {
    Hashtable<String, String> props = new Hashtable<String, String>();
    if (m_initialContextFactory != null) {
      props.put(Context.INITIAL_CONTEXT_FACTORY, m_initialContextFactory);
    }
    if (m_providerUrl != null) {
      props.put(Context.PROVIDER_URL, m_providerUrl);
    }
    if (m_userName != null && m_userName.length() > 0) {
      props.put(Context.SECURITY_PRINCIPAL, m_userName);
    }
    if (m_password != null) {
      props.put(Context.SECURITY_CREDENTIALS, m_password);
    }
    InitialContext ctx;
    try {
      if (props.size() > 0) {
        ctx = new InitialContext(props);
      }
      else {
        ctx = new InitialContext();
      }
    }
    catch (NamingException e) {
      throw new ProcessingException(e.getMessage(), e.getCause());
    }
    return ctx;
  }

  public long getCrc() {
    CRC32 crc = new CRC32();
    if (m_connectionFactoryJndiName != null) {
      crc.update(m_connectionFactoryJndiName.getBytes());
    }
    if (m_initialContextFactory != null) {
      crc.update(m_initialContextFactory.getBytes());
    }
    if (m_jndiName != null) {
      crc.update(m_jndiName.getBytes());
    }
    if (m_password != null) {
      crc.update(m_password.getBytes());
    }
    if (m_userName != null) {
      crc.update(m_userName.getBytes());
    }
    if (m_providerUrl != null) {
      crc.update(m_providerUrl.getBytes());
    }

    return crc.getValue();
  }

  @Override
  public int hashCode() {
    int h = 0;
    if (m_connectionFactoryJndiName != null) {
      h = h ^ m_connectionFactoryJndiName.hashCode();
    }
    if (m_initialContextFactory != null) {
      h = h ^ m_initialContextFactory.hashCode();
    }
    if (m_jndiName != null) {
      h = h ^ m_jndiName.hashCode();
    }
    if (m_password != null) {
      h = h ^ m_password.hashCode();
    }
    if (m_providerUrl != null) {
      h = h ^ m_providerUrl.hashCode();
    }
    if (m_userName != null) {
      h = h ^ m_userName.hashCode();
    }
    return h;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof JmsJndiConfig)) {
      return false;
    }
    return compareTo(obj) == 0;
  }

  private int compareTo(Object obj) {
    JmsJndiConfig c = (JmsJndiConfig) obj;
    if (CompareUtility.compareTo(c.getConnectionFactoryJndiName(), this.m_connectionFactoryJndiName) == 0 &&
        CompareUtility.compareTo(c.getInitialContextFactory(), this.m_initialContextFactory) == 0 &&
        CompareUtility.compareTo(c.getJndiName(), this.m_jndiName) == 0 &&
        CompareUtility.compareTo(c.getPassword(), this.m_password) == 0 &&
        CompareUtility.compareTo(c.getProviderUrl(), this.m_providerUrl) == 0 &&
        CompareUtility.compareTo(c.getUserName(), this.m_userName) == 0) {
      return 0;
    }
    else {
      return -1;
    }
  }
}
