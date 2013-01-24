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
import org.eclipse.scout.rt.ui.rap.window.IRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.window.dialog.RwtScoutDialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Default RWT busy handler for a {@link IClientSession}
 * 
 * @author imo
 * @since 3.8
 */
public class WaitForBlockingJob extends BusyJob {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(WaitForBlockingJob.class);

  private List<IRwtScoutPart> m_parts;

  public WaitForBlockingJob(String name, RwtBusyHandler handler) {
    super(name, handler);
  }

  @Override
  protected RwtBusyHandler getBusyHandler() {
    return (RwtBusyHandler) super.getBusyHandler();
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
            m_parts = findAffectedParts();
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
        display.asyncExec(new Runnable() {
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

  @Override
  protected void runBlocking(IProgressMonitor monitor) {
    //schedule blocking job
    new BlockPartsJob(getName(), getBusyHandler(), m_parts).schedule();
  }

  /**
   * @return all affected parts in the same swt / scout environment (user session). The first part is the active part
   *         and may be null.
   */
  protected List<IRwtScoutPart> findAffectedParts() {
    ArrayList<IRwtScoutPart> candidateParts = new ArrayList<IRwtScoutPart>();
    for (IRwtScoutPart part : getBusyHandler().getUiEnvironment().getOpenFormParts()) {
      if (isDialogPart(part) || isViewOrEditorPart(part)) {
        candidateParts.add(part);
      }
    }
    ArrayList<IRwtScoutPart> affectedParts = new ArrayList<IRwtScoutPart>();
    //find an active dialog, it would be the only affected item
    for (IRwtScoutPart part : candidateParts) {
      if (part.isActive() && isDialogPart(part)) {
        affectedParts.add(part);
        return affectedParts;
      }
    }
    //find a visible dialog, it would be the only affected item
    for (IRwtScoutPart part : candidateParts) {
      if (part.isActive() && isDialogPart(part)) {
        affectedParts.add(part);
        return affectedParts;
      }
    }
    //find an active view, also all other views are affected
    for (IRwtScoutPart part : candidateParts) {
      if (part.isActive() && isViewOrEditorPart(part)) {
        affectedParts.addAll(candidateParts);
        affectedParts.remove(part);
        affectedParts.add(0, part);
        return affectedParts;
      }
    }
    //all views are affected, none shows a cancel button
    affectedParts.add(null);
    affectedParts.addAll(candidateParts);
    return affectedParts;
  }

  public static boolean isDialogPart(IRwtScoutPart part) {
    return (part instanceof RwtScoutDialog);
  }

  public static boolean isViewOrEditorPart(IRwtScoutPart part) {
    return part != null && !part.getClass().getSimpleName().contains("Popup");
  }

  protected List<IRwtScoutPart> getParts() {
    return m_parts;
  }

}
