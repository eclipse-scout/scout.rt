/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.session;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Interface for runnables that are executed within an eclipse {@link Job}.
 * 
 * @since 3.8.1
 */
public interface IJobRunnable {

  /**
   * Method executed within a job.
   * 
   * @param monitor
   *          the progress monitor
   * @return Returns the job's result.
   */
  IStatus run(IProgressMonitor monitor);
}
