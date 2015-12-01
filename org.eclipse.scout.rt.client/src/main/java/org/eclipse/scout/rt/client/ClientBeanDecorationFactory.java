package org.eclipse.scout.rt.client;

import org.eclipse.scout.rt.client.services.TunnelToServerBeanDecorator;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.SimpleBeanDecorationFactory;
import org.eclipse.scout.rt.platform.interceptor.IBeanDecorator;
import org.eclipse.scout.rt.shared.TunnelToServer;

/**
 * Default client-side {@link IBeanDecorationFactory} used in {@link IPlatform#getBeanManager()}
 */
@Replace
public class ClientBeanDecorationFactory extends SimpleBeanDecorationFactory {

  @Override
  public <T> IBeanDecorator<T> decorate(IBean<T> bean, Class<? extends T> queryType) {
    if (bean.getBeanAnnotation(TunnelToServer.class) != null) {
      return decorateWithTunnelToServer(bean, queryType);
    }
    return super.decorate(bean, queryType);
  }

  protected <T> IBeanDecorator<T> decorateWithTunnelToServer(IBean<T> bean, Class<? extends T> queryType) {
    return new TunnelToServerBeanDecorator<T>(queryType);
  }
}
