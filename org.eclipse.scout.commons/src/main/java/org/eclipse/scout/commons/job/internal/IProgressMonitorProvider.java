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
package org.eclipse.scout.commons.job.internal;

import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IProgressMonitor;

/**
 * Objects of this type can provide you with a {@link IProgressMonitor}.
 */
public interface IProgressMonitorProvider {

  /**
   * Creates a {@link IProgressMonitorProvider} for the given {@link IFuture}.
   *
   * @param future
   *          {@link IFuture}
   * @return {@link IProgressMonitor}; must not be <code>null</code>.
   */
  <RESULT> IProgressMonitor create(IFuture<RESULT> future);
}
