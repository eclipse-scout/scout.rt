/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.jersey.client;

import java.util.concurrent.ExecutorService;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.glassfish.jersey.client.ClientAsyncExecutor;
import org.glassfish.jersey.spi.ExecutorServiceProvider;

/**
 * Provides the common Scout {@link JobManager} {@link ExecutorService} to be used by Jersey clients for asynchronous
 * REST calls.
 */
@ClientAsyncExecutor
public class ScoutJobExecutorServiceProvider implements ExecutorServiceProvider {

  @Override
  public ExecutorService getExecutorService() {
    return BEANS.get(JobManager.class).getExecutor();
  }

  @Override
  public void dispose(ExecutorService executorService) {
    // NOP
  }
}
