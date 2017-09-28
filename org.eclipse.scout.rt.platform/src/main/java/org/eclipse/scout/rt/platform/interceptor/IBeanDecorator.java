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
package org.eclipse.scout.rt.platform.interceptor;

import org.eclipse.scout.rt.platform.IBeanDecorationFactory;

/**
 * Interface for bean decorations used in {@link IBeanDecorationFactory}.
 */
@FunctionalInterface
public interface IBeanDecorator<T> {

  /**
   * Callback executed when a method is called on a bean.
   * 
   * @param context
   *          The context of the method call. Contains information about the method and the arguments being invoked.
   * @return the result of the call
   */
  Object invoke(IBeanInvocationContext<T> context);
}
