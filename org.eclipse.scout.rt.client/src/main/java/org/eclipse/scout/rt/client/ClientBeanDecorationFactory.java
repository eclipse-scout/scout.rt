package org.eclipse.scout.rt.client;

import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.client.services.TunnelToServerBeanInterceptor;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.SimpleBeanDecorationFactory;
import org.eclipse.scout.rt.platform.interceptor.IBeanInterceptor;
import org.eclipse.scout.rt.shared.TunnelToServer;

/**
 * Default client-side {@link IBeanDecorationFactory} used in {@link IPlatform#getBeanManager()}
 */
@Replace
public class ClientBeanDecorationFactory extends SimpleBeanDecorationFactory {

  @Override
  public <T> IBeanInterceptor<T> decorate(IBean<T> bean, Class<T> queryType) {
    if (bean.getBeanAnnotation(TunnelToServer.class) != null) {
      return decorateWithTunnelToServer(bean, queryType);
    }
    return super.decorate(bean, queryType);
  }

  protected <T> IBeanInterceptor<T> decorateWithTunnelToServer(IBean<T> bean, Class<T> queryType) {
    return new TunnelToServerBeanInterceptor<T>(queryType);
  }
}
