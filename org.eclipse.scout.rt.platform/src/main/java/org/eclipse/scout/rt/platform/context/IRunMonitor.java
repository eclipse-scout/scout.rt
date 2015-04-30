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
package org.eclipse.scout.rt.platform.context;

import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * The {@link IRunMonitor} is active during the phase of a {@link RunContext#run(org.eclipse.scout.commons.IRunnable)}
 * <p>
 * Its sole purpose is to control the cancel of an active run phase or job (if running inside a job).
 *
 * @since 5.0
 */
public interface IRunMonitor extends ICancellable {
  /**
   * The {@link IFuture} which is currently associated with the current thread.
   */
  ThreadLocal<IRunMonitor> CURRENT = new ThreadLocal<>();

  @Override
  boolean cancel(boolean interruptIfRunning);

  @Override
  boolean isCancelled();

  void registerCancellable(ICancellable c);

  void unregisterCancellable(ICancellable c);

}
