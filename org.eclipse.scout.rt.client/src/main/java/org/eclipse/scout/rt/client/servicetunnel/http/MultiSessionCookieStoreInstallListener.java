/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.servicetunnel.http;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.shared.servicetunnel.http.MultiSessionCookieStoreInstaller;

public class MultiSessionCookieStoreInstallListener implements IPlatformListener {

  private MultiSessionCookieStoreInstaller m_multiSessionCookieStoreInstaller;

  public boolean isActive() {
    return false;
  }

  @Override
  public void stateChanged(PlatformEvent event) {
    if (!isActive()) {
      return;
    }
    if (event.getState() == State.BeanManagerPrepared) {
      // Install cookie handler for HTTP based service tunnels
      m_multiSessionCookieStoreInstaller = BEANS.get(MultiSessionCookieStoreInstaller.class);
      m_multiSessionCookieStoreInstaller.install();
    }
    else if (event.getState() == State.PlatformStopped) {
      m_multiSessionCookieStoreInstaller.uninstall();
      m_multiSessionCookieStoreInstaller = null;
    }
  }
}
