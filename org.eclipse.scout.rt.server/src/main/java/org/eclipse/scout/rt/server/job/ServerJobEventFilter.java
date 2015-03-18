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
package org.eclipse.scout.rt.server.job;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;

/**
 * Filter to accept events that belong to server jobs.
 *
 * @since 5.1
 */
public class ServerJobEventFilter implements IFilter<JobEvent> {

  public static final ServerJobEventFilter INSTANCE = new ServerJobEventFilter();

  private ServerJobEventFilter() {
  }

  @Override
  public boolean accept(final JobEvent event) {
    return ServerJobs.isServerJob(event.getFuture());
  }
}
