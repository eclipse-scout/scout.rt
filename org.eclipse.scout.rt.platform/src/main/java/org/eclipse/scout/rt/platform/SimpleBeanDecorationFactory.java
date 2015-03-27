package org.eclipse.scout.rt.platform;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.platform.interceptor.IBeanInterceptor;

/**
 * Default simple {@link IBeanDecorationFactory} used in {@link IPlatform#getBeanContext()}
 */
@Priority(-10)
public class SimpleBeanDecorationFactory implements IBeanDecorationFactory {

  @Override
  public <T> IBeanInterceptor<T> decorate(IBean<T> bean, Class<T> queryType) {
    return decorateDefault(bean, queryType);
  }

  protected <T> IBeanInterceptor<T> decorateDefault(IBean<T> bean, Class<T> queryType) {
    return null;
  }

}
