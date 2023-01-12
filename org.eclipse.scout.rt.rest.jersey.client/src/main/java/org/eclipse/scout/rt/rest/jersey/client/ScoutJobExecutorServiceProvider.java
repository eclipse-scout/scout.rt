/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
