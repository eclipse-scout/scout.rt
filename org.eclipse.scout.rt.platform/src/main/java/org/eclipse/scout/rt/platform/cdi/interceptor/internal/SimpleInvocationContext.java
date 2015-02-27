/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.cdi.interceptor.internal;

import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.interceptor.InvocationContext;

/**
 *
 */
public class SimpleInvocationContext implements InvocationContext {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SimpleInvocationContext.class);

  private final Object m_target;
  private final Method m_targetMethod;
  private final Method m_proceed;
  private Object[] m_parameters;
  private Map<String, Object> m_contextData;

  public SimpleInvocationContext(Object target, Method targetMethod, Method proceed, Object[] parameters) {
    m_target = target;
    m_targetMethod = targetMethod;
    m_proceed = proceed;
    m_parameters = parameters;
  }

  @Override
  public Object getTarget() {
    return m_target;
  }

  @Override
  public Method getMethod() {
    return m_targetMethod;
  }

  @Override
  public Object[] getParameters() {
    return m_parameters;
  }

  @Override
  public void setParameters(Object[] params) {
    m_parameters = params;
  }

  @Override
  public Map<String, Object> getContextData() {
    return m_contextData;
  }

  public void setContextData(Map<String, Object> contextData) {
    m_contextData = contextData;

  }

  @Override
  public Object proceed() throws Exception {
    try {
      m_proceed.setAccessible(true);
      return m_proceed.invoke(getTarget(), getParameters());
    }
    catch (Exception ex) {
      LOG.error(String.format("Could not invoke method '%s' on '%s'. ", m_proceed.getName(), getTarget().getClass().getName()));
      throw ex;
    }
  }
}
