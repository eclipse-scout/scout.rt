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
package org.eclipse.scout.rt.ui.swt.busy.strategy.workbench;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.busy.BusyJob;
import org.eclipse.scout.rt.ui.swt.busy.SwtBusyHandler;
import org.eclipse.scout.rt.ui.swt.busy.SwtBusyUtility;
import org.eclipse.swt.widgets.Display;

/**
 * Shows the workbench progress indicator while blocking.
 * 
 * @author imo
 * @since 3.8
 */
public class BlockWorkbenchJob extends BusyJob {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BlockWorkbenchJob.class);

  public BlockWorkbenchJob(String name, SwtBusyHandler handler) {
    super(name, handler);
    setSystem(true);
  }

  @Override
  protected SwtBusyHandler getBusyHandler() {
    return (SwtBusyHandler) super.getBusyHandler();
  }

  @Override
  protected void runBusy(IProgressMonitor monitor) {
    //nop
  }

  @Override
  protected void runBlocking(final IProgressMonitor monitor) {
    final Display display = getBusyHandler().getDisplay();
    IRunnableWithProgress blockingRunnable = new IRunnableWithProgress() {
      @Override
      public void run(IProgressMonitor monitor2) throws InvocationTargetException, InterruptedException {
        BlockWorkbenchJob.super.runBlocking(monitor2);
      }
    };
    SwtBusyUtility.showWorkbenchIndicator(display, blockingRunnable);
  }

}
