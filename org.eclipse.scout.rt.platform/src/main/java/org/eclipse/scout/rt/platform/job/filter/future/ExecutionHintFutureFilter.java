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

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter to accept Futures which are tagged with a specific execution hint.
 *
 * @since 5.2
 */
public class ExecutionHintFutureFilter implements IFilter<IFuture<?>> {

  private final String m_hint;

  public ExecutionHintFutureFilter(final String hint) {
    m_hint = hint;
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return future.containsExecutionHint(m_hint);
  }
}
