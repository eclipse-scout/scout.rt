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
package org.eclipse.scout.rt.platform.job.filter.future;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.util.CompareUtility;

/**
 * Filter which accepts all Futures that belong to the given mutex object.
 *
 * @since 5.1
 */
public class MutexFutureFilter implements IFilter<IFuture<?>> {

  private final IMutex m_mutex;

  public MutexFutureFilter(final IMutex mutexObject) {
    m_mutex = mutexObject;
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return CompareUtility.equals(m_mutex, future.getJobInput().getMutex());
  }
}
