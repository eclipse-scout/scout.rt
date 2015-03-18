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
package org.eclipse.scout.rt.platform.job.filter;

import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter which discards all Futures which do not belong to the given <code>job-id</code>.
 *
 * @since 5.1
 */
public class JobFutureFilter implements IFilter<IFuture<?>> {

  private final Set<String> m_ids;

  public JobFutureFilter(final String... id) {
    m_ids = CollectionUtility.hashSet(id);
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return m_ids.contains(future.getJobInput().getId());
  }
}
