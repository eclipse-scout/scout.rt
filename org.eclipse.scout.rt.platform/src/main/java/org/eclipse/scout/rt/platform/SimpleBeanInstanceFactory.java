package org.eclipse.scout.rt.platform;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.annotations.Priority;

/**
 * Default simple {@link IBeanInstanceFactory} used in {@link IPlatform#getBeanContext()}
 */
@Priority(-10)
public class SimpleBeanInstanceFactory implements IBeanInstanceFactory {

  public SimpleBeanInstanceFactory() {
  }

  @Override
  public <T> T select(Class<T> queryClass, List<IBean<T>> regs) {
    for (IBean<?> reg : regs) {
      return createDefaultInterceptor(queryClass, reg);
    }
    return null;
  }

  @Override
  public <T> List<T> selectAll(Class<T> queryClass, List<IBean<T>> regs) {
    //TODO imo add context around queryClass (interface)
    ArrayList<T> result = new ArrayList<T>();
    for (IBean<?> reg : regs) {
      T instance;
      instance = createDefaultInterceptor(queryClass, reg);
      //add
      if (instance != null) {
        result.add(instance);
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  protected <T> T createDefaultInterceptor(Class<T> queryClass, IBean reg) {
    return (T) reg.createInstance();
  }
}
