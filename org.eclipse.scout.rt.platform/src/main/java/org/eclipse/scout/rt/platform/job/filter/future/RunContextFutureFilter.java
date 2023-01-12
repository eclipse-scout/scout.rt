/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.filter.future;

import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter to accept all Futures running in the context of the given {@link RunContext}.
 *
 * @since 5.2
 */
public class RunContextFutureFilter implements Predicate<IFuture<?>> {

  private final Class<? extends RunContext> m_runContextClazz;

  public RunContextFutureFilter(final Class<? extends RunContext> runContextClazz) {
    m_runContextClazz = runContextClazz;
  }

  @Override
  public boolean test(final IFuture<?> future) {
    final RunContext runContext = future.getJobInput().getRunContext();
    if (runContext == null) {
      return false;
    }

    return m_runContextClazz.isAssignableFrom(runContext.getClass());
  }
}
