package org.eclipse.scout.rt.servicetunnel;

import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.PlatformException;
import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;
import org.eclipse.scout.rt.servicetunnel.http.MultiSessionCookieStoreInstaller;

@ApplicationScoped
public class ServiceTunnelModule implements IPlatformListener {
  private MultiSessionCookieStoreInstaller m_multiSessionCookieStoreInstaller;

  @Override
  public void stateChanged(PlatformEvent event) throws PlatformException {
    if (event.getState() == IPlatform.State.PlatformInit) {
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
