package org.eclipse.scout.rt.platform.interceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.FinalValue;

/**
 * <h3>{@link DecoratingProxy}</h3> Proxy implementation that can wrap around instances. It automatically handles the
 * methods equals(), hashCode() and toString() and delegates to the wrapped instance.
 *
 * @author Matthias Villiger
 */
public class DecoratingProxy<INSTANCE> {

  private final Class[] m_interfaces;
  private final FinalValue<INSTANCE> m_cachedProxyInstance;
  private final FinalValue<INSTANCE> m_cachedTargetInstance;
  private final IInstanceInvocationHandler<INSTANCE> m_handler;
  private final Callable<INSTANCE> m_targetInstanceProvider;

  /**
   * Creates a new {@link DecoratingProxy} for the given interface classes which delegates all method calls to the given
   * {@link IInstanceInvocationHandler}. The instance parameter of this handler will receive {@code null}.
   *
   * @param handler
   *          The {@link IInstanceInvocationHandler} callback.
   * @param interfaces
   *          The interface classes the created proxy should implement.
   * @return The created decorating proxy.
   */
  public static <T> DecoratingProxy<T> newInstance(IInstanceInvocationHandler<T> handler, Class... interfaces) {
    return newInstance(handler, new Callable<T>() {
      @Override
      public T call() throws Exception {
        return null;
      }
    }, interfaces);
  }

  /**
   * Creates a new {@link DecoratingProxy} for the given interface classes which delegates all method calls to the given
   * {@link IInstanceInvocationHandler}. The instance parameter of this handler will receive the instance provided by
   * the given {@link Callable}. The given interfaces should be super interfaces of the instance provided by the
   * {@link Callable}.
   *
   * @param handler
   *          The {@link IInstanceInvocationHandler} callback.
   * @param targetInstanceProvider
   *          The instance provider {@link Callable}.
   * @param interfaces
   *          the interfaces the proxy should implement. Should be super interfaces of the instance provided by the
   *          targetInstanceProvider.
   * @return The created decorating proxy.
   */
  public static <T> DecoratingProxy<T> newInstance(IInstanceInvocationHandler<T> handler, Callable<T> targetInstanceProvider, Class... interfaces) {
    return new DecoratingProxy<T>(handler, targetInstanceProvider, interfaces);
  }

  protected DecoratingProxy(IInstanceInvocationHandler<INSTANCE> handler, Callable<INSTANCE> targetInstanceProvider, Class[] interfaces) {
    m_interfaces = Assertions.assertNotNull(interfaces).clone();
    m_cachedProxyInstance = new FinalValue<>();
    m_cachedTargetInstance = new FinalValue<>();
    m_handler = Assertions.assertNotNull(handler);
    m_targetInstanceProvider = Assertions.assertNotNull(targetInstanceProvider);
  }

  protected INSTANCE getTargetInstance() {
    return m_cachedTargetInstance.setIfAbsent(m_targetInstanceProvider);
  }

  protected boolean isInstanceEqualTo(Object other, INSTANCE myInstance) {
    if (other == null) {
      return false;
    }
    if (other == getProxy()) {
      return true;
    }

    boolean isProxy = Proxy.isProxyClass(other.getClass());
    if (!isProxy) {
      // argument is not a proxy. can only be equal to our instance
      return other.equals(myInstance);
    }

    Object handler = Proxy.getInvocationHandler(other);
    if (!(handler instanceof P_InvocationHandler)) {
      return false; // cannot be equals because it is another invocation handler (unknown proxy)
    }

    DecoratingProxy<?> otherProxy = ((P_InvocationHandler<?>) handler).getDecoratingProxy();
    Object otherInstance = otherProxy.getTargetInstance();
    if (otherInstance == null && myInstance == null) {
      // just compare the proxy classes because there is no real bean instance to delegate
      return Arrays.equals(getInterfaceClasses(), otherProxy.getInterfaceClasses());
    }
    if (otherInstance == myInstance) {
      return true;
    }
    return Objects.equals(myInstance, otherInstance);
  }

  protected Object invokeImpl(Method method, Object[] args) throws Throwable {
    INSTANCE instance = getTargetInstance();

    if (Object.class == method.getDeclaringClass()) {
      if ("hashCode".equals(method.getName())) {
        if (instance == null) {
          return Arrays.hashCode(getInterfaceClasses());
        }
        return instance.hashCode();
      }
      if ("equals".equals(method.getName())) {
        return isInstanceEqualTo(args[0], instance);
      }
      if ("toString".equals(method.getName())) {
        StringBuilder b = new StringBuilder("{proxy} ");
        if (instance != null) {
          b.append(instance.toString());
        }
        else {
          b.append(Arrays.toString(getInterfaceClasses()));
        }
        return b.toString();
      }
      // the other methods of Object.class are not routed to the invocation handler
    }

    return m_handler.invoke(instance, method, args);
  }

  /**
   * Gets the inner proxy to be called. Each call to the proxy will be delegated to the
   * {@link IInstanceInvocationHandler} associated with this {@link DecoratingProxy}. Exceptions are
   * {@link Object#hashCode()}, {@link Object#equals(Object)} and {@link Object#toString()} which are directly handled
   * and delegated to the target instance if available.
   *
   * @return The proxy to be called.
   */
  public INSTANCE getProxy() {
    return m_cachedProxyInstance.setIfAbsent(new Callable<INSTANCE>() {
      @Override
      @SuppressWarnings("unchecked")
      public INSTANCE call() throws Exception {
        return (INSTANCE) Proxy.newProxyInstance(DecoratingProxy.class.getClassLoader(), getInterfaceClasses(), new P_InvocationHandler(DecoratingProxy.this));
      }
    });
  }

  /**
   * @return All interface classes this proxy implements.
   */
  public Class[] getInterfaceClasses() {
    return m_interfaces.clone();
  }

  protected static final class P_InvocationHandler<T> implements InvocationHandler {

    private final DecoratingProxy<T> m_decoratingProxy;

    protected P_InvocationHandler(DecoratingProxy<T> proxy) {
      m_decoratingProxy = proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return getDecoratingProxy().invokeImpl(method, args);
    }

    DecoratingProxy<T> getDecoratingProxy() {
      return m_decoratingProxy;
    }
  }

}
