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

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.busy.BusyJob;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

/**
 * Default SWT busy handler for a {@link IClientSession}
 * <p>
 * Show busy, schedule a block job when necessary
 * 
 * @author imo
 * @since 3.8
 */
public class SwtBusyWaitJob extends BusyJob {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtBusyWaitJob.class);

  private Collection<ISwtScoutPart> m_parts;

  public SwtBusyWaitJob(String name, SwtBusyHandler handler) {
    super(name, handler);
  }

  @Override
  protected SwtBusyHandler getBusyHandler() {
    return (SwtBusyHandler) super.getBusyHandler();
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    if (getBusyHandler().isBusy()) {
      //only run busy
      runBusy(monitor);
      if (getBusyHandler().isBusy()) {
        //schedule blocking job
        new SwtBusyBlockJob(getName(), getBusyHandler(), m_parts).schedule();
      }
    }
    return Status.OK_STATUS;
  }

  /**
   * Show a wait cursor until long operation timeout
   */
  @Override
  protected void runBusy(final IProgressMonitor monitor) {
    final Display display = getBusyHandler().getDisplay();
    display.syncExec(new Runnable() {
      @Override
      public void run() {
        m_parts = findParts();
        BusyIndicator.showWhile(display, new Runnable() {
          @Override
          public void run() {
            SwtBusyWaitJob.super.runBusy(monitor);
          }
        });
      }
    });
  }

  /**
   * @return all parts in the same swt / scout environment (user session)
   */
  protected Collection<ISwtScoutPart> findParts() {
    return getBusyHandler().getSwtEnvironment().getOpenFormParts();
  }
}
