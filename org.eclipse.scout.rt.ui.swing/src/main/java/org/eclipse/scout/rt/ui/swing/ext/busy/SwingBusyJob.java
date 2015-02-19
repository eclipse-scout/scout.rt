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
package org.eclipse.scout.rt.ui.swing.ext.busy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.rt.client.busy.BusyJob;
import org.eclipse.scout.rt.client.busy.IBusyHandler;
import org.eclipse.scout.rt.ui.swing.Activator;

/**
 * Swing default shows the status bar using {@link IWorkbenchWindow#run(boolean, boolean, IRunnableWithProgress)} when
 * no
 * modal dialog is visible.
 * <p>
 * Otherwise {@link ProgressMonitorDialog#run(boolean, boolean, IRunnableWithProgress)} is called.
 * <p>
 * To enable/disable the manager just call {@link SwingBusyJob#install()}.
 * <p>
 * The handler is by default active on {@link Activator}.
 * 
 * @author imo
 * @since 3.8
 */
public class SwingBusyJob extends BusyJob {

  public SwingBusyJob(String name, IBusyHandler handler) {
    super(name, handler);
    setSystem(true);
  }

  @Override
  protected IStatus run(final IProgressMonitor monitor) {
    SwingBusyIndicator.getInstance().showWhile(new Runnable() {
      @Override
      public void run() {
        SwingBusyJob.super.run(monitor);
      }
    });
    return Status.OK_STATUS;
  }

  @Override
  protected void runBlocking(IProgressMonitor monitor) {
    SwingBusyIndicator.getInstance().startBlocking(monitor);
    super.runBlocking(monitor);
  }
}
