/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.interceptor;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.IBean;

/**
 * Intercepted method context
 */
public interface IBeanInvocationContext<T> {
  /**
   * @return the target object
   */
  IBean<T> getTargetBean();

  /**
   * @return the target object
   */
  T getTargetObject();

  /**
   * @return the target method
   */
  Method getTargetMethod();

  /**
   * @return the modifiable target arguments
   */
  Object[] getTargetArgs();

  /**
   * Call the target method
   *
   * @return the result of the call
   */
  Object proceed();
}
