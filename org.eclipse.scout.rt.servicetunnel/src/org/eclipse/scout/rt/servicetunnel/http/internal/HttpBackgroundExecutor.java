/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.servicetunnel.http.internal;

import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;

/**
 * This class is a composite of a Job and an HttpBackgroundExecutable instance.
 * 
 * @author awe
 */
class HttpBackgroundExecutor implements IHttpBackgroundExecutor {

  private final JobEx m_job;

  private final HttpBackgroundExecutable m_executable;

  HttpBackgroundExecutor(JobEx job, HttpBackgroundExecutable executable) {
    m_job = job;
    m_executable = executable;
  }

  @Override
  public JobEx getJob() {
    return m_job;
  }

  @Override
  public IServiceTunnelResponse getResponse() {
    return m_executable.getResponse();
  }

}
