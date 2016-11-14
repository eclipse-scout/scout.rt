package org.eclipse.scout.rt.client.servicetunnel.http;

import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.shared.servicetunnel.http.MultiSessionCookieStoreInstaller;

public class MultiSessionCookieStoreInstallListener implements IPlatformListener {

  private MultiSessionCookieStoreInstaller m_multiSessionCookieStoreInstaller;

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == IPlatform.State.BeanManagerPrepared) {
      // Install cookie handler for HTTP based service tunnels
      m_multiSessionCookieStoreInstaller = new MultiSessionCookieStoreInstaller();
      m_multiSessionCookieStoreInstaller.install();
    }
    else if (event.getState() == IPlatform.State.PlatformStopped) {
      m_multiSessionCookieStoreInstaller.uninstall();
      m_multiSessionCookieStoreInstaller = null;
    }
  }
}
