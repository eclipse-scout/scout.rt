package org.eclipse.scout.rt.platform.cdi;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.platform.IPlatform;

/**
 * Default simple {@link IBeanInstanceFactory} used in {@link IPlatform#getBeanContext()}
 */
@Priority(-10)
public class SimpleBeanInstanceFactory implements IBeanInstanceFactory {

  public SimpleBeanInstanceFactory() {
  }

  @Override
  public <T> T select(Class<T> queryClass, SortedSet<IBeanRegistration> regs) {
    for (IBeanRegistration<?> reg : regs) {
      return createDefaultInterceptor(queryClass, reg);
    }
    return null;
  }

  @Override
  public <T> List<T> selectAll(Class<T> queryClass, SortedSet<IBeanRegistration> regs) {
    //TODO imo add context around queryClass (interface)
    ArrayList<T> result = new ArrayList<T>();
    for (IBeanRegistration<?> reg : regs) {
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
  protected <T> T createDefaultInterceptor(Class<T> queryClass, IBeanRegistration reg) {
    return (T) reg.getInstance();
  }
}
