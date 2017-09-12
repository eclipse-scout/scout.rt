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
