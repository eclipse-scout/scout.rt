/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.busy.strategy.simple;

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
 * Only show busy
 * 
 * @author imo
 * @since 3.8
 */
public class ShowBusyJob extends BusyJob {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ShowBusyJob.class);

  public ShowBusyJob(String name, SwtBusyHandler handler) {
    super(name, handler);
  }

  @Override
  protected SwtBusyHandler getBusyHandler() {
    return (SwtBusyHandler) super.getBusyHandler();
  }

  @Override
  protected void runBusy(IProgressMonitor monitor) {
    IRunnableWithProgress busyRunnable = new IRunnableWithProgress() {
      @Override
      public void run(IProgressMonitor monitor2) throws InvocationTargetException, InterruptedException {
        ShowBusyJob.super.runBusy(monitor2);
      }
    };
    final Display display = getBusyHandler().getDisplay();
    SwtBusyUtility.showBusyIndicator(display, busyRunnable, monitor);
  }

  @Override
  protected void runBlocking(IProgressMonitor monitor) {
    IRunnableWithProgress busyRunnable = new IRunnableWithProgress() {
      @Override
      public void run(IProgressMonitor monitor2) throws InvocationTargetException, InterruptedException {
        ShowBusyJob.super.runBlocking(monitor2);
      }
    };
    final Display display = getBusyHandler().getDisplay();
    SwtBusyUtility.showBusyIndicator(display, busyRunnable, monitor);
  }

}
