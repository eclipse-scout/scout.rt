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
package org.eclipse.scout.rt.ui.rap.mobile.busy;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.busy.BusyJob;
import org.eclipse.scout.rt.ui.rap.busy.RwtBusyHandler;
import org.eclipse.scout.rt.ui.rap.window.IRwtScoutPart;
import org.eclipse.swt.widgets.Display;

public class BusyBlockDialogJob extends BusyJob {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BusyBlockDialogJob.class);
  private final List<IRwtScoutPart> m_parts;

  public BusyBlockDialogJob(String name, RwtBusyHandler handler, List<IRwtScoutPart> parts) {
    super(name, handler);
    setSystem(true);
    m_parts = parts;
  }

  @Override
  protected RwtBusyHandler getBusyHandler() {
    return (RwtBusyHandler) super.getBusyHandler();
  }

  @Override
  protected void runBusy(IProgressMonitor monitor) {
    //nop
  }

  @Override
  protected void runBlocking(final IProgressMonitor monitor) {
    if (m_parts == null || m_parts.size() == 0) {
      return;
    }
    final Display display = getBusyHandler().getDisplay();
    final Holder<BusyBlockDialog> dialogHolder = new Holder<BusyBlockDialog>(BusyBlockDialog.class);
    try {
      display.syncExec(new Runnable() {
        @Override
        public void run() {
          IRwtScoutPart activePart = m_parts.get(0);
          if (activePart.getUiContainer() == null || activePart.getUiContainer().isDisposed()) {
            return;
          }
          BusyBlockDialog dialog = new BusyBlockDialog(activePart.getUiContainer().getShell(), getBusyHandler().getUiEnvironment(), monitor);
          dialog.open();
          dialogHolder.setValue(dialog);
        }
      });
      //
      BusyBlockDialogJob.super.runBlocking(monitor);
      //
    }
    finally {
      display.syncExec(new Runnable() {
        @Override
        public void run() {
          BusyBlockDialog dialog = dialogHolder.getValue();
          if (dialog != null) {
            dialog.close();
          }
        }
      });
    }
  }

}
