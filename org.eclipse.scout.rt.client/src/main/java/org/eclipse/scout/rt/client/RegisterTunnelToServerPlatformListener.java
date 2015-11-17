package org.eclipse.scout.rt.client;

import java.util.List;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.eclipse.scout.rt.shared.SharedConfigProperties.CreateTunnelToServerBeansProperty;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default client-side {@link IBeanDecorationFactory} used in {@link IPlatform#getBeanManager()}
 */
public class RegisterTunnelToServerPlatformListener implements IPlatformListener {
  private static final Logger LOG = LoggerFactory.getLogger(RegisterTunnelToServerPlatformListener.class);

  @Override
  public void stateChanged(PlatformEvent event) throws PlatformException {
    if (event.getState() == IPlatform.State.BeanManagerPrepared) {
      if (!CONFIG.getPropertyValue(CreateTunnelToServerBeansProperty.class)) {
        return;
      }
      //register all tunnels to server
      final IBeanManager beanManager = event.getSource().getBeanManager();
      final IClassInventory classInventory = ClassInventory.get();
      registerTunnelToServerProxies(beanManager, classInventory);
    }
  }

  protected void registerTunnelToServerProxies(final IBeanManager beanManager, final IClassInventory classInventory) {
    for (IClassInfo ci : classInventory.getKnownAnnotatedTypes(TunnelToServer.class)) {
      if (!ci.isInterface() || !ci.isPublic()) {
        LOG.error("The annotation @" + TunnelToServer.class.getSimpleName() + " can only be used on public interfaces, not on " + ci.name());
        continue;
      }

      try {
        Class<?> c = ci.resolveClass();
        registerTunnelToServerProxy(beanManager, c);
      }
      catch (Exception e) {
        LOG.warn("loading class", e);
      }
    }
  }

  protected void registerTunnelToServerProxy(final IBeanManager beanManager, Class<?> c) {
    if (!acceptClass(beanManager, c)) {
      LOG.debug("ignoring class [{}]", c);
      return;
    }

    beanManager.registerBean(createBeanMetaData(c));
  }

  /**
   * Returns <code>true</code> if the given class (a public interface) should be registered as bean.
   */
  protected boolean acceptClass(IBeanManager beanManager, Class<?> beanClass) {
    List<? extends IBean<?>> beans = beanManager.getBeans(beanClass);
    for (IBean<?> bean : beans) {
      if (!bean.getBeanClazz().isInterface()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Creates a new {@link BeanMetaData} for the given class.
   */
  protected BeanMetaData createBeanMetaData(Class<?> c) {
    return new BeanMetaData(c).withApplicationScoped(false);
  }
}
