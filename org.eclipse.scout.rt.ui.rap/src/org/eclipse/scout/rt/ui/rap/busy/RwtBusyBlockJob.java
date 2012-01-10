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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.busy.BusyJob;
import org.eclipse.scout.rt.ui.rap.core.window.IRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.window.dialog.RwtScoutDialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Default RWT busy handler for a {@link IClientSession}
 * 
 * @author imo
 * @since 3.8
 */
public class RwtBusyBlockJob extends BusyJob {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtBusyBlockJob.class);

  private final Collection<IRwtScoutPart> m_parts;

  public RwtBusyBlockJob(String name, RwtBusyHandler handler, Collection<IRwtScoutPart> parts) {
    super(name, handler);
    setSystem(true);
    m_parts = parts;
  }

  @Override
  protected RwtBusyHandler getBusyHandler() {
    return (RwtBusyHandler) super.getBusyHandler();
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    if (getBusyHandler().isBusy()) {
      runBlocking(monitor);
    }
    return Status.OK_STATUS;
  }

  /**
   * Show a stop button in the active form parts header section.
   * <p>
   * Do not show a wait cursor.
   */
  @Override
  protected void runBlocking(final IProgressMonitor monitor) {
    if (m_parts == null || m_parts.size() == 0) {
      return;
    }
    final ArrayList<RwtScoutPartBusyDecorator> decoList = new ArrayList<RwtScoutPartBusyDecorator>();
    final Display display = getBusyHandler().getDisplay();
    final Control busyControl = (Control) getBusyHandler().getUiEnvironment().getClientSession().getData(RwtBusyHandler.BUSY_CONTROL_CLIENT_SESSION_KEY);
    try {
      display.syncExec(new Runnable() {
        @Override
        public void run() {
          if (busyControl != null && !busyControl.isDisposed()) {
            busyControl.setVisible(true);
          }
          IRwtScoutPart ap = getActivePart(m_parts);
          for (IRwtScoutPart p : m_parts) {
            decoList.add(new RwtScoutPartBusyDecorator(p, p == ap, getBusyHandler().getUiEnvironment()));
          }
          for (RwtScoutPartBusyDecorator deco : decoList) {
            try {
              deco.attach(monitor);
            }
            catch (Exception e1) {
              LOG.warn("attach", e1);
            }
          }
        }
      });
      //
      RwtBusyBlockJob.super.runBlocking(monitor);
      //
    }
    finally {
      display.syncExec(new Runnable() {
        @Override
        public void run() {
          if (busyControl != null && !busyControl.isDisposed()) {
            busyControl.setVisible(false);
          }
          for (RwtScoutPartBusyDecorator deco : decoList) {
            try {
              deco.detach();
            }
            catch (Exception e1) {
              LOG.warn("detach", e1);
            }
          }
        }
      });
    }
  }

  protected IRwtScoutPart getActivePart(Collection<IRwtScoutPart> parts) {
    //find dialog
    for (IRwtScoutPart part : parts) {
      if (part instanceof RwtScoutDialog && part.isActive()) {
        return part;
      }
    }
    //find view/editor
    for (IRwtScoutPart part : parts) {
      if (!part.getClass().getSimpleName().contains("Popup") && part.isActive()) {
        return part;
      }
    }
    return null;
  }

}
