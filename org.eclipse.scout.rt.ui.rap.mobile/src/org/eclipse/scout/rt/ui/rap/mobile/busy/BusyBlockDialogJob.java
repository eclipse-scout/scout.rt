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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.client.busy.BusyJob;
import org.eclipse.scout.rt.ui.rap.busy.RwtBusyHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class BusyBlockDialogJob extends BusyJob {

  public BusyBlockDialogJob(String name, RwtBusyHandler handler) {
    super(name, handler);
    setSystem(true);
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
    final Display display = getBusyHandler().getDisplay();
    final Holder<BusyBlockDialog> dialogHolder = new Holder<BusyBlockDialog>(BusyBlockDialog.class);
    try {
      display.syncExec(new Runnable() {
        @Override
        public void run() {
          Shell parentShell = getBusyHandler().getUiEnvironment().getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS);
          if (parentShell == null || parentShell.isDisposed()) {
            return;
          }
          BusyBlockDialog dialog = new BusyBlockDialog(parentShell, getBusyHandler().getUiEnvironment(), monitor);
          dialog.open();
          dialogHolder.setValue(dialog);
        }
      });
      //
      BusyBlockDialogJob.super.runBlocking(monitor);
      //
    }
    finally {
      display.asyncExec(new Runnable() {
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
