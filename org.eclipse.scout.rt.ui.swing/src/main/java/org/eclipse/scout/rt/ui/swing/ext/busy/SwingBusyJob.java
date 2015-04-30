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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.busy.BusyJob;
import org.eclipse.scout.rt.client.busy.IBusyHandler;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Swing default shows the status bar using {@link IWorkbenchWindow#run(boolean, boolean, IRunnableWithProgress)} when
 * no
 * modal dialog is visible.
 * <p>
 * Otherwise {@link ProgressMonitorDialog#run(boolean, boolean, IRunnableWithProgress)} is called.
 * <p>
 * To enable/disable the manager just call {@link SwingBusyJob#install()}.
 * <p>
 *
 * @author imo
 * @since 3.8
 */
public class SwingBusyJob extends BusyJob {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingBusyJob.class);

  public SwingBusyJob(IBusyHandler handler) {
    super(handler);
  }

  @Override
  public void run() throws Exception {
    SwingBusyIndicator.getInstance().showWhile(new Runnable() {
      @Override
      public void run() {
        try {
          SwingBusyJob.super.run();
        }
        catch (Exception e) {
          LOG.error("", e);
        }
      }
    });
  }

  @Override
  protected void runBlocking() {
    SwingBusyIndicator.getInstance().startBlocking(IFuture.CURRENT.get());
    super.runBlocking();
  }
}
