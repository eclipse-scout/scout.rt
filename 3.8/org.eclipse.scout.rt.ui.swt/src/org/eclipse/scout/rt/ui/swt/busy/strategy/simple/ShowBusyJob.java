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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.busy.BusyJob;
import org.eclipse.scout.rt.ui.swt.busy.SwtBusyHandler;
import org.eclipse.scout.rt.ui.swt.busy.SwtBusyUtility;

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
  protected IStatus run(IProgressMonitor monitor) {
    IRunnableWithProgress busyRunnable = new IRunnableWithProgress() {
      @Override
      public void run(IProgressMonitor monitor2) throws InvocationTargetException, InterruptedException {
        ShowBusyJob.super.run(monitor2);
      }
    };
    SwtBusyHandler busyHandler = getBusyHandler();
    SwtBusyUtility.showBusyIndicator(busyHandler, busyRunnable, monitor);
    return Status.OK_STATUS;
  }

}
