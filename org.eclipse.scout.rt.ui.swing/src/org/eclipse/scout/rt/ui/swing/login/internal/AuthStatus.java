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
package org.eclipse.scout.rt.ui.swing.login.internal;

import java.net.URL;

public class AuthStatus {
  private Boolean m_ok;
  private String m_username;
  private String m_password;
  private URL m_url;
  private boolean m_proxy;
  private boolean m_savePassword;

  public boolean isOk() {
    return m_ok != null && m_ok == true;
  }

  public boolean isCancel() {
    return m_ok != null && m_ok == false;
  }

  public void setOk() {
    m_ok = true;
  }

  public void setCancel() {
    m_ok = false;
  }

  public String getUsername() {
    return m_username;
  }

  public void setUsername(String username) {
    m_username = username;
  }

  public String getPassword() {
    return m_password;
  }

  public void setPassword(String password) {
    m_password = password;
  }

  public URL getUrl() {
    return m_url;
  }

  public void setUrl(URL url) {
    m_url = url;
  }

  public boolean isProxy() {
    return m_proxy;
  }

  public void setProxy(boolean proxy) {
    m_proxy = proxy;
  }

  public boolean isSavePassword() {
    return m_savePassword;
  }

  public void setSavePassword(boolean savePassword) {
    m_savePassword = savePassword;
  }

}
