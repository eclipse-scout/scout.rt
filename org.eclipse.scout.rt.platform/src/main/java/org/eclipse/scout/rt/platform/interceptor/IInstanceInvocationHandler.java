package org.eclipse.scout.rt.platform.interceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * <h3>{@link IInstanceInvocationHandler}</h3> Invocation handler that receives the instance to use in a call instead of
 * the proxy as used in {@link InvocationHandler}.<br>
 * {@link IInstanceInvocationHandler}s are e.g. used as callback in {@link DecoratingProxy}.
 *
 * @author Matthias Villiger
 * @see DecoratingProxy
 */
@FunctionalInterface
public interface IInstanceInvocationHandler<T> {
  /**
   * Processes a method invocation on a {@link DecoratingProxy} instance and returns the result. This method will be
   * invoked on an invocation handler when a method is invoked on a proxy instance that it is associated with.
   *
   * @param instance
   *          The instance on which the given {@link Method} could be called. May be {@code null} if no predefined
   *          instance is available.
   * @param method
   *          the {@code Method} instance corresponding to the interface method invoked on the proxy instance. The
   *          declaring class of the {@code Method} object will be the interface that the method was declared in, which
   *          may be a superinterface of the proxy interface that the proxy class inherits the method through.
   * @param args
   *          an array of objects containing the values of the arguments passed in the method invocation on the proxy
   *          instance, or {@code null} if interface method takes no arguments. Arguments of primitive types are wrapped
   *          in instances of the appropriate primitive wrapper class, such as {@code java.lang.Integer} or
   *          {@code java.lang.Boolean}.
   * @return the value to return from the method invocation on the proxy instance. If the declared return type of the
   *         interface method is a primitive type, then the value returned by this method must be an instance of the
   *         corresponding primitive wrapper class; otherwise, it must be a type assignable to the declared return type.
   *         If the value returned by this method is {@code null} and the interface method's return type is primitive,
   *         then a {@code NullPointerException} will be thrown by the method invocation on the proxy instance. If the
   *         value returned by this method is otherwise not compatible with the interface method's declared return type
   *         as described above, a {@code ClassCastException} will be thrown by the method invocation on the proxy
   *         instance.
   * @throws Throwable
   *           the exception to throw from the method invocation on the proxy instance. The exception's type must be
   *           assignable either to any of the exception types declared in the {@code throws} clause of the interface
   *           method or to the unchecked exception types {@code java.lang.RuntimeException} or {@code java.lang.Error}.
   *           If a checked exception is thrown by this method that is not assignable to any of the exception types
   *           declared in the {@code throws} clause of the interface method, then an
   *           {@link UndeclaredThrowableException} containing the exception that was thrown by this method will be
   *           thrown by the method invocation on the proxy instance.
   * @see UndeclaredThrowableException
   * @see DecoratingProxy
   */
  @SuppressWarnings("squid:S00112")
  Object invoke(T instance, Method method, Object[] args) throws Throwable;
}
