/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.context;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.TypeParameterBeanRegistry;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;

/**
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
