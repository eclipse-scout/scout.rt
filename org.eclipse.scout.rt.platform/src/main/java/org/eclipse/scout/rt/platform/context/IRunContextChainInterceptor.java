package org.eclipse.scout.rt.platform.context;

import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;

/**
 * <h3>{@link IRunContextChainInterceptor}</h3><br>
 * A {@link IRunContextChainInterceptor} should always be produced of a {@link IRunContextChainInterceptorProducer} to
 * add a certain variable (e.g. ThreadLocal) to a {@link RunContext}.
 */
public interface IRunContextChainInterceptor<RESULT> extends ICallableInterceptor<RESULT> {

  /**
   * is called when from {@link RunContext#fillEmpty()} to apply default values to the current {@link RunContext}. This
   * method is called in the caller environment most likely a outer {@link RunContext}.
   */
  void fillEmtpy();

  /**
   * is called from {@link RunContext#fillCurrentValues()} to apply current environment variables to the current
   * {@link RunContext}. This method is called in the caller environment most likely a outer {@link RunContext}.
   */
  void fillCurrent();

}
