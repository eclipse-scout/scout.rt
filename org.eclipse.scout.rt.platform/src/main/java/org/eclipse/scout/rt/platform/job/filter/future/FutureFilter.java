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

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IAdaptable;

/**
 * Filter which accepts all of the given Futures.
 *
 * @since 5.1
 */
public class FutureFilter implements Predicate<IFuture<?>>, IAdaptable {

  private final Set<IFuture<?>> m_futures;

  public FutureFilter(final IFuture<?>... futures) {
    m_futures = CollectionUtility.hashSet(futures);
  }

  public FutureFilter(final Collection<IFuture<?>> futures) {
    m_futures = CollectionUtility.hashSet(futures);
  }

  @Override
  public boolean test(final IFuture<?> future) {
    return m_futures.contains(future);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(final Class<T> type) {
    if (type == IFuture[].class) {
      return (T) m_futures.toArray(new IFuture[0]);
    }
    return null;
  }
}
