/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Factory for creating {@link ServerJob}s
 *
 * @since 4.2
 */
public interface IServerJobFactory {

  /**
   * Creates a new ServerJob for a runnable
   *
   * @param name
   *          jobName
   * @param runnable
   * @return {@link ServerJob}
   */
  ServerJob create(String name, final ITransactionRunnable runnable);

  /**
   * Creates and runs a {@link ServerJob} blocking without progress monitor.
   *
   * @param name
   *          jobName
   * @param runnable
   * @throws ProcessingException
   */
  ServerJob runNow(String name, final ITransactionRunnable runnable) throws ProcessingException;

}
