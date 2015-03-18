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
package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IProgressMonitor;

/**
 * Default implementation of {@link IProgressMonitor}.
 */
public class ProgressMonitor implements IProgressMonitor {

  private final IFuture<?> m_future;

  public ProgressMonitor(final IFuture<?> future) {
    m_future = Assertions.assertNotNull(future);
  }

  @Override
  public boolean isCancelled() {
    return m_future.isCancelled();
  }

  @Override
  public boolean cancel(final boolean interruptIfRunning) {
    return m_future.cancel(interruptIfRunning);
  }
}
