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
package org.eclipse.scout.rt.ui.swt;

/*
 * Tray example snippet: place an icon with a popup menu on the system tray
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 *
 * @since 3.0
 */
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public final class TestTrayIcon {

  private TestTrayIcon() {
  }

  public static void main(String[] args) {
    final Display display = new Display();
    Image image = new Image(display, 16, 16);
    final Tray tray = display.getSystemTray();
    if (tray == null) {
      System.out.println("The system tray is not available");
      System.exit(0);
    }
    final TrayItem item = new TrayItem(tray, SWT.NONE);
    item.setImage(image);

    final Shell shell2 = new Shell(display);
    final AtomicBoolean on = new AtomicBoolean(true);

    new Job("") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        display.asyncExec(new Runnable() {
          @Override
          public void run() {
            if (on.get()) {
              ToolTip tip = new ToolTip(shell2, SWT.BALLOON | SWT.ICON_INFORMATION);
              tip.setMessage("AAA " + System.currentTimeMillis());
              item.setToolTip(tip);
              tip.setVisible(true);
            }
            else {
              ToolTip tip = new ToolTip(shell2, SWT.NONE);
              item.setToolTip(tip);
              tip.setVisible(true);
            }
            on.set(!on.get());
            schedule(1000);
          }
        });
        return Status.OK_STATUS;
      }
    }.schedule(3000);

    Shell shell = new Shell(display);
    shell.setBounds(50, 50, 300, 200);
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    image.dispose();
    display.dispose();
  }
}
