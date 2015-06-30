package org.eclipse.scout.rt.client;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.shared.TunnelToServer;

/**
 * Default client-side {@link IBeanDecorationFactory} used in {@link IPlatform#getBeanManager()}
 */
public class RegisterTunnelToServerPlatformListener implements IPlatformListener {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RegisterTunnelToServerPlatformListener.class);

  @Override
  public void stateChanged(PlatformEvent event) throws PlatformException {
    if (event.getState() == IPlatform.State.BeanManagerPrepared) {
      //register all tunnels to server
      for (IClassInfo ci : ClassInventory.get().getKnownAnnotatedTypes(TunnelToServer.class)) {
        if (!ci.isPublic()) {
          continue;
        }
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
        if (!event.getSource().getBeanManager().getBeans(c).isEmpty()) {
          continue;
        }
        event.getSource().getBeanManager().registerBean(new BeanMetaData(c).applicationScoped(false));
      }
    }
  }
}
