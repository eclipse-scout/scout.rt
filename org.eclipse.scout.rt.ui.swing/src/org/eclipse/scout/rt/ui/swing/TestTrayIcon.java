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
package org.eclipse.scout.rt.ui.swing;

/*
 * Tray example snippet: place an icon with a popup menu on the system tray
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 *
 * @since 3.0
 */
import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public final class TestTrayIcon {

  private TestTrayIcon() {
  }

  public static void main(String[] args) throws InterruptedException, AWTException {
    final TrayIcon trayItem = new TrayIcon(new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB));
    SystemTray.getSystemTray().add(trayItem);
    final AtomicBoolean on = new AtomicBoolean(true);
    new Job("") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            if (on.get()) {
              trayItem.displayMessage("Title", "Text\nMore Text", TrayIcon.MessageType.INFO);
            }
            else {
              //nop
            }
            on.set(!on.get());
            schedule(1000);
          }
        });
        return Status.OK_STATUS;
      }
    }.schedule(3000);

    while (true) {
      Thread.sleep(100000L);
    }

  }
}
