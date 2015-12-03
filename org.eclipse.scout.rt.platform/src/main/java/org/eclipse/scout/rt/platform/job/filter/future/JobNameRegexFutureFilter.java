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

import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter which accepts all Futures which have a name matching the regex.
 *
 * @since 5.1
 */
public class JobNameRegexFutureFilter implements IFilter<IFuture<?>> {

  private final Pattern m_regex;

  public JobNameRegexFutureFilter(final Pattern regex) {
    m_regex = regex;
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    if (future.getJobInput().getName() == null) {
      return false;
    }
    return m_regex.matcher(future.getJobInput().getName()).matches();
  }
}
