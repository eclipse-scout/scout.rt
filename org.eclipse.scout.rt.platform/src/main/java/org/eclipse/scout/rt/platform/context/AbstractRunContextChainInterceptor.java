package org.eclipse.scout.rt.platform.context;

/**
 * <h3>{@link AbstractRunContextChainInterceptor}</h3>
 *
 * @author aho
 */
public abstract class AbstractRunContextChainInterceptor<RESULT> implements IRunContextChainInterceptor<RESULT> {

  @Override
  public void fillCurrent() {
  }

  @Override
  public void fillEmtpy() {
  }
}
