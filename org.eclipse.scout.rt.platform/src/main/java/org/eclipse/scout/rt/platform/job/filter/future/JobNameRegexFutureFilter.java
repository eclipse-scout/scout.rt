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
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter which accepts all Futures which have a name matching the regex.
 *
 * @since 5.1
 */
public class JobNameRegexFutureFilter implements Predicate<IFuture<?>> {

  private final Pattern m_regex;

  public JobNameRegexFutureFilter(final Pattern regex) {
    m_regex = regex;
  }

  @Override
  public boolean test(final IFuture<?> future) {
    if (future.getJobInput().getName() == null) {
      return false;
    }
    return m_regex.matcher(future.getJobInput().getName()).matches();
  }
}
