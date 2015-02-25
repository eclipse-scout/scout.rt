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
package org.eclipse.scout.commons.job;

import org.eclipse.scout.commons.job.Executables.IExecutable;

/**
 * Represents a runnable to be given to a job manager for execution.
 *
 * @see Runnable
 * @see 5.1
 */
public interface IRunnable extends IExecutable<Void> {

  /**
   * Computes a result, or throws an exception if unable to do so.
   *
   * @throws Exception
   */
  void run() throws Exception;
}
