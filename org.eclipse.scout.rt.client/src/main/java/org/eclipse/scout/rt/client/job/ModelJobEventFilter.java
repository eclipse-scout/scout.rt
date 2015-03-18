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
package org.eclipse.scout.rt.client.job;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;

/**
 * Filter to accept events that belong to model jobs.
 *
 * @since 5.1
 */
public class ModelJobEventFilter implements IFilter<JobEvent> {

  public static final ModelJobEventFilter INSTANCE = new ModelJobEventFilter();

  private ModelJobEventFilter() {
  }

  @Override
  public boolean accept(final JobEvent event) {
    return ModelJobs.isModelJob(event.getFuture());
  }
}
