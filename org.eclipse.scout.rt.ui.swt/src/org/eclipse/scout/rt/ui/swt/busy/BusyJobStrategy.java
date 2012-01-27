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
package org.eclipse.scout.rt.ui.swt.busy;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.ui.swt.busy.strategy.simple.ShowBusyJob;
import org.eclipse.scout.rt.ui.swt.busy.strategy.workbench.WaitForBlockingJob;

/**
 * Creates a busy visualizing job
 * 
 * @author imo
 * @since 3.8
 */
public final class BusyJobStrategy {
  private BusyJobStrategy() {
  }

  public static Job createSimpleJob(SwtBusyHandler handler, boolean abortable) {
    Job job = new ShowBusyJob(TEXTS.get("BusyJob"), handler);
    job.setSystem(!abortable);
    return job;
  }

  public static Job createWorkbenchJob(SwtBusyHandler handler, boolean allowWorkbenchBlocking) {
    return new WaitForBlockingJob(TEXTS.get("BusyJob"), handler, allowWorkbenchBlocking);
  }

}
