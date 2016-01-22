package org.eclipse.scout.rt.client;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.interceptor.DecoratingProxy;
import org.eclipse.scout.rt.platform.interceptor.IInstanceInvocationHandler;
import org.eclipse.scout.rt.platform.util.VerboseUtility;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h3>{@link TunnelToServerProxyProducer}</h3>
 *
 * @author Matthias Villiger
 */
public class TunnelToServerProxyProducer<T> implements IBeanInstanceProducer<T>, IInstanceInvocationHandler<T> {

  private static final Logger LOG = LoggerFactory.getLogger(TunnelToServerProxyProducer.class);

  private final DecoratingProxy<T> m_proxy;

  private final Class<?> m_interfaceClass;

  public TunnelToServerProxyProducer(Class<?> interfaceClass) {
    m_interfaceClass = interfaceClass;
    m_proxy = DecoratingProxy.newInstance(this, interfaceClass);
  }

  @Override
  public T produce(IBean<T> bean) {
    return m_proxy.getProxy();
  }

  @Override
  public Object invoke(T instance /*will always be null*/, Method method, Object[] args) throws Throwable {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Tunnel call to {}.{}({})", getInterfaceClass(), method.getName(), VerboseUtility.dumpObjects(args));
    }

    return BEANS.get(IServiceTunnel.class).invokeService(getInterfaceClass(), method, args);
  }

  protected Class<?> getInterfaceClass() {
    return m_interfaceClass;
  }
}
