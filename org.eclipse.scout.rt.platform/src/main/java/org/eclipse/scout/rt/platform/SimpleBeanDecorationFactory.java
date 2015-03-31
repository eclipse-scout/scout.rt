package org.eclipse.scout.rt.platform;

import org.eclipse.scout.rt.platform.interceptor.IBeanInterceptor;

/**
 * Default simple {@link IBeanDecorationFactory} used in {@link IPlatform#getBeanContext()}
 */
public class SimpleBeanDecorationFactory implements IBeanDecorationFactory {

  @Override
  public <T> IBeanInterceptor<T> decorate(IBean<T> bean, Class<T> queryType) {
    return null;
  }

}
