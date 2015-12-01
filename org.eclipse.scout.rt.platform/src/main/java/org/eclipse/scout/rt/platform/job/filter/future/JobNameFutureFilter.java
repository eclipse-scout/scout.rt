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

import java.util.Set;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Filter which accepts all Futures which do belong to the given job names.
 *
 * @since 5.1
 */
public class JobNameFutureFilter implements IFilter<IFuture<?>> {

  private final Set<String> m_names;

  public JobNameFutureFilter(final String... names) {
    m_names = CollectionUtility.hashSet(names);
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return m_names.contains(future.getJobInput().getName());
  }
}
