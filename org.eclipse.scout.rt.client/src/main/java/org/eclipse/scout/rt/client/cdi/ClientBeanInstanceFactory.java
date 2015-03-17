package org.eclipse.scout.rt.client.cdi;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.scout.rt.client.Client;
import org.eclipse.scout.rt.client.services.ClientServiceTunnelInvocationHandler;
import org.eclipse.scout.rt.platform.IBeanInstanceFactory;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.internal.IBeanRegistration;
import org.eclipse.scout.rt.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.shared.TunnelToServer;

/**
 * Default client-side {@link IBeanInstanceFactory} used in {@link IPlatform#getBeanContext()}
 */
public class ClientBeanInstanceFactory implements IBeanInstanceFactory {

  @Override
  public <T> T select(Class<T> queryClass, SortedSet<IBeanRegistration> regs) {
    for (IBeanRegistration<?> reg : regs) {
      if (reg.getBean().getBeanAnnotation(TunnelToServer.class) != null) {
        return createTunnelInterceptor(queryClass, reg);
      }
      else if (reg.getBean().getBeanAnnotation(Client.class) != null) {
        return createClientInterceptor(queryClass, reg);
      }
      else {
        return createDefaultInterceptor(queryClass, reg);
      }
    }
    return null;
  }

  @Override
  public <T> List<T> selectAll(Class<T> queryClass, SortedSet<IBeanRegistration> regs) {
    //TODO imo add context around queryClass (interface)
    ArrayList<T> result = new ArrayList<T>();
    for (IBeanRegistration<?> reg : regs) {
      T instance;
      if (reg.getBean().getBeanAnnotation(TunnelToServer.class) != null) {
        instance = createTunnelInterceptor(queryClass, reg);
      }
      else if (reg.getBean().getBeanAnnotation(Client.class) != null) {
        instance = createClientInterceptor(queryClass, reg);
      }
      else {
        instance = createDefaultInterceptor(queryClass, reg);
      }
      //add
      if (instance != null) {
        result.add(instance);
      }
    }
    return result;
  }

  protected <T> T createTunnelInterceptor(Class<T> queryClass, IBeanRegistration reg) {
    //TODO imo add context around queryClass (interface)
    return ServiceTunnelUtility.createProxy(queryClass, new ClientServiceTunnelInvocationHandler(queryClass));
  }

  @SuppressWarnings("unchecked")
  protected <T> T createClientInterceptor(Class<T> queryClass, IBeanRegistration reg) {
    //TODO imo add context around queryClass (interface)
    return (T) reg.getInstance();
  }

  @SuppressWarnings("unchecked")
  protected <T> T createDefaultInterceptor(Class<T> queryClass, IBeanRegistration reg) {
    return (T) reg.getInstance();
  }
}
