package org.eclipse.scout.rt.platform.context;

import javax.annotation.PostConstruct;
import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * <h3>{@link IRunContextChainInterceptorProducer}</h3><br>
 * Any instance of {@link IRunContextChainInterceptorProducer} will be added to the {@link RunContext} level defined as
 * generic parameter T. The producer is used to add additional information to a certain {@link RunContext} like
 * {@link ThreadLocal} variables or {@link Subject}.
 */
@ApplicationScoped
public interface IRunContextChainInterceptorProducer<T extends RunContext> {

  /**
   * This method is called during initialization ( {@link PostConstruct}) of a {@link RunContext} or any of its
   * subclasses. And must return a {@link IRunContextChainInterceptor} which will be added to the {@link RunContext}.
   *
   * @return
   */
  <RESULT> IRunContextChainInterceptor<?, RESULT> create();

}
