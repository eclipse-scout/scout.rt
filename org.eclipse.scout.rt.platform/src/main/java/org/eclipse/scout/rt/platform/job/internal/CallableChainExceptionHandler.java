package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.chain.callable.CallableChain.Chain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;
import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h3>{@link CallableChainExceptionHandler}</h3>
 * <p>
 * This exception handler is only for infrastructure reasons. It handles exceptions occurring in
 * {@link ICallableDecorator} and {@link ICallableInterceptor} which are before {@link ExceptionProcessor} in the
 * callable chain. Exceptions handled by the {@link ExceptionProcessor} will not be handled of this handler.
 */
public class CallableChainExceptionHandler<RESULT> implements ICallableInterceptor<RESULT> {

  private static final Logger LOG = LoggerFactory.getLogger(CallableChainExceptionHandler.class);

  @Override
  public RESULT intercept(Chain<RESULT> chain) throws Exception {
    try {
      return chain.continueChain();
    }
    // do not handle exceptions twice.
    catch (CallableChainHandledException e) { // NOSONAR
      if (e.getCause() instanceof Error) {
        throw (Error) e.getCause();
      }
      else if (e.getCause() instanceof Exception) {
        throw (Exception) e.getCause();
      }
      else {
        throw e;
      }
    }
    catch (Exception | Error err) { // NOSONAR
      LOG.warn("Exception in callable chain.", err);
      throw err;
    }
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
