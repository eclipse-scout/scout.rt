package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;
import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;
import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * <h3>{@link CallableChainHandledException}</h3>
 * <p>
 * This exception is used to mark exceptions through all {@link ICallableDecorator} and {@link ICallableInterceptor} as
 * handled by the {@link ExceptionProcessor}.
 */
public class CallableChainHandledException extends Exception {
  private static final long serialVersionUID = 1L;

  public CallableChainHandledException(Throwable original) {
    super(original.getMessage(), original);
    if (!(original instanceof Error || original instanceof Exception)) {
      Assertions.fail("{} is not an instance of Error or Exceptions, others are not allowed.", original);
    }
  }

}
