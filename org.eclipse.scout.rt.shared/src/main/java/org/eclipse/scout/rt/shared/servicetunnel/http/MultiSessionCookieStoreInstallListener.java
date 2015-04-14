package org.eclipse.scout.rt.shared.servicetunnel.http;

import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.shared.TierState;
import org.eclipse.scout.rt.shared.TierState.Tier;

@ApplicationScoped
public class MultiSessionCookieStoreInstallListener implements IPlatformListener {
  private MultiSessionCookieStoreInstaller m_multiSessionCookieStoreInstaller;

  @Override
  public void stateChanged(PlatformEvent event) throws PlatformException {
    if (TierState.get() == Tier.BackEnd) {
      return;
    }
    if (event.getState() == IPlatform.State.PlatformInit) {
      // Install cookie handler for HTTP based service tunnels
      m_multiSessionCookieStoreInstaller = new MultiSessionCookieStoreInstaller();
      String enabledText = ConfigIniUtility.getProperty(MultiSessionCookieStoreInstaller.PROP_MULTI_SESSION_COOKIE_STORE_ENABLED);
      if ("true".equalsIgnoreCase(enabledText)) {
        m_multiSessionCookieStoreInstaller.install();
      }
      else if ("false".equalsIgnoreCase(enabledText)) {
        //nop
      }
      else {
        m_multiSessionCookieStoreInstaller.check();
      }
    }
    else if (event.getState() == IPlatform.State.PlatformStopped) {
      m_multiSessionCookieStoreInstaller.uninstall();
      m_multiSessionCookieStoreInstaller = null;
    }
  }
}
