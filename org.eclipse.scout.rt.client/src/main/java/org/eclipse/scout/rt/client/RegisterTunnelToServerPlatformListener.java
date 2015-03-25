package org.eclipse.scout.rt.client;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BeanData;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.PlatformException;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.shared.TunnelToServer;

/**
 * Default client-side {@link IBeanDecorationFactory} used in {@link IPlatform#getBeanContext()}
 */
public class RegisterTunnelToServerPlatformListener implements IPlatformListener {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RegisterTunnelToServerPlatformListener.class);

  @Override
  public void stateChanged(PlatformEvent event) throws PlatformException {
    if (event.getState() == IPlatform.State.BeanContextPrepared) {
      //register all tunnels to server
      for (IClassInfo ci : event.getSource().getClassInventory().getKnownAnnotatedTypes(TunnelToServer.class)) {
        if (!ci.isInterface()) {
          LOG.error("The annotation @" + TunnelToServer.class.getSimpleName() + " can only be used on interfaces, not on " + ci.name());
          continue;
        }
        Class<?> c;
        try {
          c = ci.resolveClass();
        }
        catch (Exception e) {
          LOG.warn("loading class", e);
          continue;
        }
        if (!event.getSource().getBeanContext().getBeans(c).isEmpty()) {
          continue;
        }
        event.getSource().getBeanContext().registerBean(new BeanData(c).applicationScoped(false));
      }
    }
  }
}
