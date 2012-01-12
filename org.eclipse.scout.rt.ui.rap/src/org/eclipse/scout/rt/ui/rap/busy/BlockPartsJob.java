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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
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
public class BlockPartsJob extends BusyJob {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BlockPartsJob.class);

  private final List<IRwtScoutPart> m_parts;

  public BlockPartsJob(String name, RwtBusyHandler handler, List<IRwtScoutPart> parts) {
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
    final ArrayList<RwtScoutPartBlockingDecorator> decoList = new ArrayList<RwtScoutPartBlockingDecorator>();
    final Display display = getBusyHandler().getDisplay();
    final Control busyControl = (Control) getBusyHandler().getUiEnvironment().getClientSession().getData(RwtBusyHandler.BUSY_CONTROL_CLIENT_SESSION_KEY);
    try {
      display.syncExec(new Runnable() {
        @Override
        public void run() {
          if (busyControl != null && !busyControl.isDisposed()) {
            busyControl.setVisible(true);
          }
          IRwtScoutPart activePart = m_parts.get(0);
          for (IRwtScoutPart p : m_parts) {
            if (p == null) {
              continue;
            }
            decoList.add(new RwtScoutPartBlockingDecorator(p, p == activePart, getBusyHandler().getUiEnvironment()));
          }
          for (RwtScoutPartBlockingDecorator deco : decoList) {
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
      BlockPartsJob.super.runBlocking(monitor);
      //
    }
    finally {
      display.syncExec(new Runnable() {
        @Override
        public void run() {
          if (busyControl != null && !busyControl.isDisposed()) {
            busyControl.setVisible(false);
          }
          for (RwtScoutPartBlockingDecorator deco : decoList) {
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

}
