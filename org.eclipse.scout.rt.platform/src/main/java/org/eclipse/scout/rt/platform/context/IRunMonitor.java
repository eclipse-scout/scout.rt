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

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.platform.Bean;

/**
 * The {@link IRunMonitor} is active during the phase of a {@link RunContext#run(IRunnable)} or running inside a job.
 * <p>
 * Its sole purpose is to control the cancel of an active run phase or job (if running inside a job).
 *
 * @since 5.1
 */
@Bean
public interface IRunMonitor extends ICancellable {
  /**
   * The {@link IRunMonitor} which is currently associated with the current thread; is never <code>null</code> if
   * running within a {@link RunContext} or job.
   */
  ThreadLocal<IRunMonitor> CURRENT = new ThreadLocal<>();

  /**
   * Registers the given {@link ICancellable} to be cancelled once this monitor get cancelled.
   */
  void registerCancellable(ICancellable cancellable);

  /**
   * Unregisters the given {@link ICancellable}.
   */
  void unregisterCancellable(ICancellable cancellable);

}
