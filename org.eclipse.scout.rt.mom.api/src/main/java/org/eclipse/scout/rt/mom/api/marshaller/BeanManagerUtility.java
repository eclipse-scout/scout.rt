package org.eclipse.scout.rt.mom.api.marshaller;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.Replace;

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
   * Lookup {@code lookupClazz} in bean manager and tries to find the correct {@link Class} to instantiate considering
   * {@link Replace} and {@link Order} annotation.
   *
   * @param lookupClazz
   *          Class to lookup
   * @return Correct {@link Class} to instantiate or {@code null} if {@code lookupClazz} is not a Scout bean or not
   *         uniquely defined.
   */
  public Class<?> lookupClass(Class<?> lookupClazz) {
    List<? extends IBean<?>> beans = m_beanManager.getBeans(lookupClazz);
    if (beans.isEmpty()) {
      // CASE 1: lookupClazz is not a Scout bean, return null
      return null;
    }
    else if (beans.size() == 1) {
      // CASE 2: lookupClazz is a Scout bean and is registered once (uniquely) in bean manager, return bean class
      return beans.get(0).getBeanClazz();
    }
    else {
      IBean<?> defaultImplBean = m_beanManager.uniqueBean(lookupClazz);
      if (defaultImplBean != null) {
        // CASE 3: lookupClazz is a Scout bean and is registered more than once in bean manager, but the runtime class instance is uniquely defined (clazz could be replaced or defined using order annotation)
        return defaultImplBean.getBeanClazz();
      }
      // CASE 4: lookupClazz is a Scout bean and is registered more than once in bean manager and the runtime class instance is not uniquely defined (e.g. abstract class with two same-order implementations),
      //         Cannot lookup correct bean class to instantiate!
      return null;
    }
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
