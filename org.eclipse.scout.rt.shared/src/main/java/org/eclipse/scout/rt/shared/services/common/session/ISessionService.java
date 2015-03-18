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
package org.eclipse.scout.rt.shared.services.common.session;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.IExecutable;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobExecutionException;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.service.IService;

/**
 * @since 3.8.0
 */
@Priority(-3)
public interface ISessionService extends IService {

  /**
   * see {@link IJobManager#runNow(IExecutable)}
   */
  <RESULT> RESULT runNow(IExecutable<RESULT> executable) throws ProcessingException;

  /**
   * see {@link IJobManager#runNow(IExecutable, IJobInput)}
   */
  <RESULT> RESULT runNow(IExecutable<RESULT> executable, JobInput input) throws ProcessingException;

  /**
   * see {@link IJobManager#schedule(IExecutable)}
   */
  <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable) throws JobExecutionException;

  /**
   * see {@link IJobManager#schedule(IExecutable, IJobInput)}
   */
  <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable, JobInput input) throws JobExecutionException;

  /**
   * see {@link IJobManager#schedule(IExecutable, long, TimeUnit)}
   */
  <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable, long delay, TimeUnit delayUnit) throws JobExecutionException;

  /**
   * see {@link IJobManager#schedule(IExecutable, long, TimeUnit, IJobInput)}
   */
  <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable, long delay, TimeUnit delayUnit, JobInput input) throws JobExecutionException;

  /**
   * @return The default job input
   */
  JobInput defaults();
}
