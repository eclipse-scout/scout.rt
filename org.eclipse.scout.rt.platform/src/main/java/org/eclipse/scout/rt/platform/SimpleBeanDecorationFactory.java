package org.eclipse.scout.rt.platform;

import org.eclipse.scout.commons.annotations.Priority;

/**
 * Default simple {@link IBeanDecorationFactory} used in {@link IPlatform#getBeanContext()}
 */
@Priority(-10)
public class SimpleBeanDecorationFactory implements IBeanDecorationFactory {

  @Override
  public <T> T decorate(IBean<T> bean, T instance) {
    return decorateDefault(bean, instance);
  }

  protected <T> T decorateDefault(IBean<T> bean, T instance) {
    return instance;
  }

}
