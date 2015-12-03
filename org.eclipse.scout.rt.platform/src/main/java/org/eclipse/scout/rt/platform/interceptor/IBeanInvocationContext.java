/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
   * Gets the {@link IBean} that will be the target of this method call.<br>
   * <br>
   * <b>Important:</b><br>
   * Never call {@link IBean#getInstance()} on the result of this method!<br>
   * Use {@link #getTargetObject()} on this context instead.
   *
   * @return the target {@link IBean}. Is never <code>null</code>.
   */
  IBean<T> getTargetBean();

  /**
   * @return The target instance on which the method should be invoked. May be <code>null</code> in case
   */
  T getTargetObject();

  /**
   * @return the target method to invoke. Never returns <code>null</code>.
   */
  Method getTargetMethod();

  /**
   * @return the modifiable arguments that will be passed to the method (see {@link #getTargetMethod()}). May be
   *         <code>null</code>.
   */
  Object[] getTargetArgs();

  /**
   * Invokes the method of {@link #getTargetMethod()} on the instance of {@link #getTargetObject()} and passes the
   * arguments of {@link #getTargetArgs()}.
   *
   * @return the result of the method call. May be <code>null</code.>
   */
  Object proceed();
}
