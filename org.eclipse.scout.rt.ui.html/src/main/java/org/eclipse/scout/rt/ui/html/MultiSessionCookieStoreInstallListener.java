/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.shared.servicetunnel.http.MultiSessionCookieStoreInstaller;

@ApplicationScoped
public class MultiSessionCookieStoreInstallListener implements IPlatformListener {

  private MultiSessionCookieStoreInstaller m_multiSessionCookieStoreInstaller;

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == IPlatform.State.BeanManagerPrepared) {
      // Install cookie handler for HTTP based service tunnels
      m_multiSessionCookieStoreInstaller = BEANS.get(MultiSessionCookieStoreInstaller.class);
      m_multiSessionCookieStoreInstaller.install();
    }
    else if (event.getState() == IPlatform.State.PlatformStopped) {
      m_multiSessionCookieStoreInstaller.uninstall();
      m_multiSessionCookieStoreInstaller = null;
    }
  }
}
