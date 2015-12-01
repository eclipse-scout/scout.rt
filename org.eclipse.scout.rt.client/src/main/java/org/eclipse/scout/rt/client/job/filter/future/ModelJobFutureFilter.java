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
package org.eclipse.scout.rt.client.job.filter.future;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter to accept Futures which belong to model jobs. Those are jobs, which are running on behalf of a
 * {@link ClientRunContext}, and have the {@link IClientSession} set as their mutex object.
 * <p>
 * However, only one such model job Future is active at any time, and its executing thread is called the model thread.
 *
 * @since 5.2
 */
public class ModelJobFutureFilter implements IFilter<IFuture<?>> {

  public static final IFilter<IFuture<?>> INSTANCE = new ModelJobFutureFilter();

  private ModelJobFutureFilter() {
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return ModelJobs.isModelJob(future);
  }
}
