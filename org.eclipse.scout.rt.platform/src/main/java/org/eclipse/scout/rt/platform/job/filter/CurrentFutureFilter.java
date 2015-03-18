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

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter which discards all Futures except the current one.
 *
 * @since 5.1
 */
public class CurrentFutureFilter implements IFilter<IFuture<?>> {

  public static final IFilter<IFuture<?>> INSTANCE = new CurrentFutureFilter();

  private CurrentFutureFilter() {
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return future == IFuture.CURRENT.get();
  }
}
