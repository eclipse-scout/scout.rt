package org.eclipse.scout.rt.servicetunnel;

import org.eclipse.scout.rt.platform.IModule;
import org.eclipse.scout.rt.servicetunnel.http.MultiSessionCookieStoreInstaller;

public class ServiceTunnelModule implements IModule {

  private MultiSessionCookieStoreInstaller m_multiSessionCookieStoreInstaller;

  @Override
  public void start() {
    // Install cookie handler for HTTP based service tunnels
    m_multiSessionCookieStoreInstaller = new MultiSessionCookieStoreInstaller();
    m_multiSessionCookieStoreInstaller.install();
  }

  @Override
  public void stop() {
    m_multiSessionCookieStoreInstaller.uninstall();
    m_multiSessionCookieStoreInstaller = null;
  }
}
