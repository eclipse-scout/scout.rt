/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.context;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.TypeParameterBeanRegistry;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;

/**
 * <h3>{@link RunContextChainIntercepterRegistry}</h3>
 *
 * @author Andreas Hoegger
 */
@ApplicationScoped
public class RunContextChainIntercepterRegistry {

  private final TypeParameterBeanRegistry<IRunContextChainInterceptorProducer> m_runContextInterceptorProducers = new TypeParameterBeanRegistry<>(IRunContextChainInterceptorProducer.class);

  private IRegistrationHandle m_registrationHandle;

  @PostConstruct
  protected void buildProducerLinking() {
    m_registrationHandle = m_runContextInterceptorProducers.registerBeans(BEANS.all(IRunContextChainInterceptorProducer.class));
  }

  /**
   * only for testing reasons
   */
  public void reindex() {
    m_registrationHandle.dispose();
    buildProducerLinking();
  }

  @SuppressWarnings("unchecked")
  public <T extends RunContext> List<IRunContextChainInterceptorProducer<T>> getRunContextInterceptorProducer(Class<?> runContextClass) {
    List<IRunContextChainInterceptorProducer<T>> result = new ArrayList<>();
    List<IRunContextChainInterceptorProducer> beans = m_runContextInterceptorProducers.getBeans(runContextClass);
    for (IRunContextChainInterceptorProducer iRunContextChainInterceptorProducer : beans) {
      result.add(iRunContextChainInterceptorProducer);
    }
    return result;

  }
}
