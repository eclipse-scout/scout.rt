package org.eclipse.scout.rt.mom.api.marshaller;

import java.lang.annotation.Annotation;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.Platform;

/**
 * Utility dealing with {@link IBeanManager}
 */
// TODO [7.0] pbz: Consider merge/move utility functions to IBean / IBeanManager
@ApplicationScoped
public class BeanManagerUtility {

  protected IBeanManager m_beanManager;

  @PostConstruct
  protected void init() {
    m_beanManager = Platform.get().getBeanManager();
  }

  /**
   * @return {@code true} if {@code lookupClazz} is a registered bean, else {@code false}
   */
  public boolean isBeanClass(Class<?> lookupClazz) {
    return !m_beanManager.getBeans(lookupClazz).isEmpty();
  }

  /**
   * Lookup the corresponding {@link IBean} for specified {@code beanClazz}. The {@link IBean} is returned even if the
   * {@code beanClazz} was replaced by another bean implementation or is not the most specific bean for the specified
   * {@code beanClazz}.
   *
   * @param beanClazz
   *          Bean class to find
   * @return {@link IBean} with matching {@code beanClazz} or null if no matching bean could be found
   */
  public IBean<?> lookupRegisteredBean(Class<?> beanClazz) {
    for (IBean<?> bean : m_beanManager.getRegisteredBeans(beanClazz)) {
      if (bean.getBeanClazz() == beanClazz) {
        return bean;
      }
    }
    return null;
  }

  /**
   * @return {@code true} if the specified {@code bean} is not {@code null} and is annotated by the specified
   *         {@code annotation}, else {@code false}.
   */
  public <ANNOTATION extends Annotation> boolean hasAnnotation(IBean<?> bean, Class<ANNOTATION> annotation) {
    return bean != null && bean.getBeanAnnotation(annotation) != null;
  }
}
