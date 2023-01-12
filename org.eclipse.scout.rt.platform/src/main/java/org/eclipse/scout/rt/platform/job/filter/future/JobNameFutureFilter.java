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

import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Filter which accepts all Futures which do belong to the given job names.
 *
 * @since 5.1
 */
public class JobNameFutureFilter implements Predicate<IFuture<?>> {

  private final Set<String> m_names;

  public JobNameFutureFilter(final String... names) {
    m_names = CollectionUtility.hashSet(names);
  }

  @Override
  public boolean test(final IFuture<?> future) {
    return m_names.contains(future.getJobInput().getName());
  }
}
