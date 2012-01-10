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
package org.eclipse.scout.rt.ui.rap.busy;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.busy.BusyJob;
import org.eclipse.scout.rt.ui.rap.core.window.IRwtScoutPart;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Default RWT busy handler for a {@link IClientSession}
 * 
 * @author imo
 * @since 3.8
 */
public class RwtBusyWaitJob extends BusyJob {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtBusyWaitJob.class);

  private Collection<IRwtScoutPart> m_parts;

  public RwtBusyWaitJob(String name, RwtBusyHandler handler) {
    super(name, handler);
  }

  @Override
  protected RwtBusyHandler getBusyHandler() {
    return (RwtBusyHandler) super.getBusyHandler();
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    if (getBusyHandler().isBusy()) {
      //only run busy
      runBusy(monitor);
      if (getBusyHandler().isBusy()) {
        //schedule blocking job
        new RwtBusyBlockJob(getName(), getBusyHandler(), m_parts).schedule();
      }
    }
    return Status.OK_STATUS;
  }

  /**
   * Show a wait cursor until long operation timeout
   */
  @Override
  protected void runBusy(final IProgressMonitor monitor) {
    Display display = getBusyHandler().getDisplay();
    final Control busyControl = (Control) getBusyHandler().getUiEnvironment().getClientSession().getData(RwtBusyHandler.BUSY_CONTROL_CLIENT_SESSION_KEY);
    try {
      if (display != null && !display.isDisposed()) {
        display.syncExec(new Runnable() {
          @Override
          public void run() {
            m_parts = findParts();
            if (busyControl != null && !busyControl.isDisposed()) {
              busyControl.setVisible(true);
            }
          }
        });
      }
      //
      super.runBusy(monitor);
    }
    finally {
      if (display != null && !display.isDisposed()) {
        display.syncExec(new Runnable() {
          @Override
          public void run() {
            if (busyControl != null && !busyControl.isDisposed()) {
              busyControl.setVisible(false);
            }
          }
        });
      }
    }
  }

  protected Collection<IRwtScoutPart> findParts() {
    return getBusyHandler().getUiEnvironment().getOpenFormParts();
  }

}
