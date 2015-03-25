package org.eclipse.scout.rt.client;

import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.client.services.ClientServiceTunnelInvocationHandler;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.SimpleBeanDecorationFactory;
import org.eclipse.scout.rt.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.shared.TunnelToServer;

/**
 * Default client-side {@link IBeanDecorationFactory} used in {@link IPlatform#getBeanContext()}
 */
@Replace
public class ClientBeanDecorationFactory extends SimpleBeanDecorationFactory {

  @Override
  public <T> T decorate(IBean<T> bean, T instance) {
    if (bean.getBeanAnnotation(TunnelToServer.class) != null) {
      return decorateWithTunnelToServer(bean, instance);
    }
    if (bean.getBeanAnnotation(Client.class) != null) {
      return decorateWithClientSessionCheck(bean, instance);
    }
    return decorateDefault(bean, instance);
  }

  protected <T> T decorateWithTunnelToServer(IBean<T> bean, T instance) {
    //TODO imo add context around queryClass (interface)
    return ServiceTunnelUtility.createProxy(bean.getBeanClazz(), new ClientServiceTunnelInvocationHandler(bean.getBeanClazz()));
  }

  protected <T> T decorateWithClientSessionCheck(IBean<T> bean, T instance) {
    //TODO imo add context around queryClass (interface)
    return instance;
  }

}
