package org.eclipse.scout.rt.platform.context;

/**
 * <h3>{@link AbstractRunContextChainInterceptor}</h3>
 *
 * @author aho
 */
public abstract class AbstractRunContextChainInterceptor<T, RESULT> implements IRunContextChainInterceptor<T, RESULT> {

  @Override
  public void fillCurrent() {
  }

  @Override
  public void fillEmtpy() {
  }
}
