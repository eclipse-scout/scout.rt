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
package org.eclipse.scout.rt.platform.job;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.job.internal.ProgressMonitor;

/**
 * Objects of this type can provide a {@link IProgressMonitor}.
 */
@Bean
public class ProgressMonitorProvider  {

  /**
   * Provides a {@link IProgressMonitorProvider} for the given {@link IFuture}.
   *
   * @param future
   *          {@link IFuture}
   * @return {@link IProgressMonitor}; must not be <code>null</code>.
   */
  public <RESULT> IProgressMonitor provide(final IFuture<RESULT> future) {
    return new ProgressMonitor(future);
  }
}
