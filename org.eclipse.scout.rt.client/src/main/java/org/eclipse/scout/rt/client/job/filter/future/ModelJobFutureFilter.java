/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.job.filter.future;

import java.util.function.Predicate;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter to accept Futures which belong to model jobs.
 *
 * @since 5.2
 */
public final class ModelJobFutureFilter implements Predicate<IFuture<?>> {

  public static final Predicate<IFuture<?>> INSTANCE = new ModelJobFutureFilter();

  private ModelJobFutureFilter() {
  }

  @Override
  public boolean test(final IFuture<?> future) {
    return ModelJobs.isModelJob(future);
  }
}
