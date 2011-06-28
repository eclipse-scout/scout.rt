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
package org.eclipse.scout.rt.server.services.common.processing;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.server.services.common.jdbc.internal.exec.RunningStatementStore;
import org.eclipse.scout.rt.shared.services.common.processing.IServerProcessingCancelService;
import org.eclipse.scout.service.AbstractService;

@Priority(-1)
public class ServerProcessingCancelService extends AbstractService implements IServerProcessingCancelService {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServerProcessingCancelService.class);

  @Override
  public void cancel() {
    IServerSession session = ThreadContext.getServerSession();
    if (session == null) {
      LOG.error("failed to cancel server processing because job is not running in session context");
      return;
    }

    // cancel all running server jobs that belong to that session
    Job[] jobs = Job.getJobManager().find(ServerJob.class);
    for (Job job : jobs) {
      if (job instanceof ServerJob && ((ServerJob) job).getServerSession() == session) {
        job.cancel();
      }
    }

    // cancel running JDBC statements that belong to that session
    RunningStatementStore.cancelAll();
  }
}
