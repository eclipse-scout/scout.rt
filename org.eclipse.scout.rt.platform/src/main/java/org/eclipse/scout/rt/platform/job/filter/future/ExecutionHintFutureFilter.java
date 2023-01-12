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

import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter to accept Futures which are tagged with a specific execution hint.
 *
 * @since 5.2
 */
public class ExecutionHintFutureFilter implements Predicate<IFuture<?>> {

  private final String m_hint;

  public ExecutionHintFutureFilter(final String hint) {
    m_hint = hint;
  }

  @Override
  public boolean test(final IFuture<?> future) {
    return future.containsExecutionHint(m_hint);
  }
}
