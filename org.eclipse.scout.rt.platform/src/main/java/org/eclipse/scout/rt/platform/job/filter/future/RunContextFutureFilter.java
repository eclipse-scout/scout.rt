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
package org.eclipse.scout.rt.platform.job.filter.future;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter to accept all Futures running in the context of the given {@link RunContext}.
 *
 * @since 5.2
 */
public class RunContextFutureFilter implements IFilter<IFuture<?>> {

  private final Class<? extends RunContext> m_runContextClazz;

  public RunContextFutureFilter(final Class<? extends RunContext> runContextClazz) {
    m_runContextClazz = runContextClazz;
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    final RunContext runContext = future.getJobInput().getRunContext();
    if (runContext == null) {
      return false;
    }

    return m_runContextClazz.isAssignableFrom(runContext.getClass());
  }
}
