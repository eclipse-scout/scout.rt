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
package org.eclipse.scout.rt.server.job;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Filter which accepts Futures only if belonging to the current server session.
 *
 * @see ISession#CURRENT
 * @since 5.1
 */
public class CurrentSessionFutureFilter implements IFilter<IFuture<?>> {

  public static final IFilter<IFuture<?>> INSTANCE = new CurrentSessionFutureFilter();

  private CurrentSessionFutureFilter() {
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    final JobInput jobInput = future.getJobInput();
    if (jobInput instanceof ServerJobInput) {
      return (ISession.CURRENT.get() == ((ServerJobInput) jobInput).getSession());
    }
    else {
      return false;
    }
  }
}
