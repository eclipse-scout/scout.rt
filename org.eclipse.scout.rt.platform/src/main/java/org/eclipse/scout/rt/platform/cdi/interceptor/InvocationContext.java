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
package org.eclipse.scout.rt.platform.cdi.interceptor;

import java.lang.reflect.Method;

/**
 *
 */
public interface InvocationContext {

  Object getTarget();

  Method getMethod();

  Object[] getParameters();

  void setParameters(Object[] params);

  /**
   * Returns the context data associated with this invocation or lifecycle callback. If there is no context data, an
   * empty Map object will be returned.
   */
  java.util.Map<String, Object> getContextData();

  Object proceed() throws Exception;
}
