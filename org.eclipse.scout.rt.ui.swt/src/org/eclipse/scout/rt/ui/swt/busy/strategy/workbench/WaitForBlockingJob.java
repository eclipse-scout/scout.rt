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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.busy.BusyJob;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.ui.swt.busy.SwtBusyHandler;
import org.eclipse.scout.rt.ui.swt.busy.SwtBusyUtility;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;

/**
 * Default SWT busy handler for a {@link IClientSession}
 * <p>
 * Show busy, schedule a block job when necessary
 * 
 * @author imo
 * @since 3.8
 */
public class WaitForBlockingJob extends BusyJob {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(WaitForBlockingJob.class);

  private final boolean m_allowBlockWorkbench;
  private List<ISwtScoutPart> m_parts;
  private boolean m_blockWorkbench;

  public WaitForBlockingJob(String name, SwtBusyHandler handler, boolean allowBlockWorkbench) {
    super(name, handler);
    m_allowBlockWorkbench = allowBlockWorkbench;
  }

  @Override
  protected SwtBusyHandler getBusyHandler() {
    return (SwtBusyHandler) super.getBusyHandler();
  }

  /**
   * Show a wait cursor until long operation timeout
   */
  @Override
  protected void runBusy(IProgressMonitor monitor) {
    SwtBusyHandler busyHandler = getBusyHandler();
    busyHandler.getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        m_parts = SwtBusyUtility.findAffectedParts(getBusyHandler().getSwtEnvironment());
        m_blockWorkbench = shouldBlockWorkbench(m_parts.size() > 0 ? m_parts.get(0) : null);
      }
    });
    //
    IRunnableWithProgress busyRunnable = new IRunnableWithProgress() {
      @Override
      public void run(IProgressMonitor monitor2) throws InvocationTargetException, InterruptedException {
        WaitForBlockingJob.super.runBusy(monitor2);
      }
    };
    SwtBusyUtility.showBusyIndicator(busyHandler, busyRunnable, monitor);
  }

  @Override
  protected void runBlocking(IProgressMonitor monitor) {
    if (m_blockWorkbench) {
      new BlockWorkbenchJob(TEXTS.get("BusyJob"), getBusyHandler()).schedule();
    }
    else {
      //schedule a blocking job
      new BlockPartsJob(getName(), getBusyHandler(), m_parts).schedule();
    }
  }

  protected boolean shouldBlockWorkbench(ISwtScoutPart activePart) {
    if (!m_allowBlockWorkbench) {
      return false;
    }
    if (activePart == null || activePart.getForm() == null) {
      return true;
    }
    if (activePart.getForm().getDisplayHint() == IForm.DISPLAY_HINT_DIALOG) {
      return false;
    }
    if (activePart.getForm().getDisplayHint() == IForm.DISPLAY_HINT_POPUP_DIALOG) {
      return false;
    }
    if (activePart.getForm().getDisplayHint() == IForm.DISPLAY_HINT_POPUP_WINDOW) {
      return false;
    }
    return true;
  }

}
