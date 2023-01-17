/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel;

import java.util.List;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.eclipse.scout.rt.shared.SharedConfigProperties.CreateTunnelToServerBeansProperty;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IPlatformListener} to register proxy instances for interfaces annotated with {@link TunnelToServer}.
 */
public class RegisterTunnelToServerPlatformListener implements IPlatformListener {
  private static final Logger LOG = LoggerFactory.getLogger(RegisterTunnelToServerPlatformListener.class);

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == State.BeanManagerPrepared) {
      if (!isEnabled()) {
        return;
      }
      //register all tunnels to server
      final IBeanManager beanManager = event.getSource().getBeanManager();
      final IClassInventory classInventory = ClassInventory.get();
      registerTunnelToServerProxies(beanManager, classInventory);
    }
  }

  protected boolean isEnabled() {
    return CONFIG.getPropertyValue(CreateTunnelToServerBeansProperty.class);
  }

  protected void registerTunnelToServerProxies(final IBeanManager beanManager, final IClassInventory classInventory) {
    for (IClassInfo ci : classInventory.getKnownAnnotatedTypes(TunnelToServer.class)) {
      if (!ci.isInterface() || !ci.isPublic()) {
        LOG.error("The annotation @{} can only be used on public interfaces, not on {}", TunnelToServer.class.getSimpleName(), ci.name());
        continue;
      }

      try {
        Class<?> c = ci.resolveClass();
        registerTunnelToServerProxy(beanManager, c);
      }
      catch (Exception e) {
        LOG.warn("could not load class [{}]", ci.name(), e);
      }
    }
    LOG.info("Tunnel to server proxies registered.");
  }

  protected void registerTunnelToServerProxy(final IBeanManager beanManager, Class<?> c) {
    if (!acceptClass(beanManager, c)) {
      LOG.debug("ignoring class [{}]", c);
      return;
    }

    beanManager.registerBean(createBeanMetaData(c));
    LOG.debug("Registered [{}}", c);
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
    ServiceTunnelProxyProducer<?> tunnelProxyProducer = new ServiceTunnelProxyProducer<>(c);
    return new BeanMetaData(c).withApplicationScoped(true).withProducer(tunnelProxyProducer);
  }
}
