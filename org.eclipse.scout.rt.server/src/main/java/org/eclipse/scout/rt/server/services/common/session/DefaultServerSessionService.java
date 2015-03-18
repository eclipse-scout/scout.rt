/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.session;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.IExecutable;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobExecutionException;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.job.ServerJobs;
import org.eclipse.scout.rt.shared.services.common.session.ISessionService;
import org.eclipse.scout.service.AbstractService;

/**
 * Default implementation of {@link ISessionService} used on server-side.
 *
 * @since 3.8.1
 */
@Priority(-1)
public class DefaultServerSessionService extends AbstractService implements ISessionService {

  @Override
  public <RESULT> RESULT runNow(IExecutable<RESULT> executable) throws ProcessingException {
    return ServerJobs.runNow(executable);
  }

  @Override
  public <RESULT> RESULT runNow(IExecutable<RESULT> executable, JobInput input) throws ProcessingException {
    return ServerJobs.runNow(executable, (ServerJobInput) input);
  }

  @Override
  public <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable) throws JobExecutionException {
    return ServerJobs.schedule(executable);
  }

  @Override
  public <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable, JobInput input) throws JobExecutionException {
    return ServerJobs.schedule(executable, (ServerJobInput) input);
  }

  @Override
  public <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable, long delay, TimeUnit delayUnit) throws JobExecutionException {
    return ServerJobs.schedule(executable, delay, delayUnit);
  }

  @Override
  public <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable, long delay, TimeUnit delayUnit, JobInput input) throws JobExecutionException {
    return ServerJobs.schedule(executable, delay, delayUnit, (ServerJobInput) input);
  }

  @Override
  public JobInput defaults() {
    return ServerJobInput.defaults();
  }
}
