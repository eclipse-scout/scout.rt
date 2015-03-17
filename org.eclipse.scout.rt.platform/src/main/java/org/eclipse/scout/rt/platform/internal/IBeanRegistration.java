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
package org.eclipse.scout.rt.platform.internal;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanContext;
import org.eclipse.scout.rt.platform.IBeanInstanceFactory;

/**
 * This is the registration for one {@link IBean} in the {@link IBeanContext}
 */
public interface IBeanRegistration<T> extends Comparable<T> {

  /**
   * @return the raw, undecorated and non-intercepted value of the bean
   *         <p>
   *         used in {@link IBeanInstanceFactory}
   */
  T getInstance();

  IBean<T> getBean();

}
