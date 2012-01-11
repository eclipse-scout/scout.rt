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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.busy.BusyJob;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.scout.rt.ui.swt.window.desktop.editor.AbstractScoutEditorPart;
import org.eclipse.scout.rt.ui.swt.window.desktop.view.AbstractScoutView;
import org.eclipse.scout.rt.ui.swt.window.dialog.SwtScoutDialog;
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
public class SwtBusyStep1Job extends BusyJob {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtBusyStep1Job.class);

  private List<ISwtScoutPart> m_parts;

  public SwtBusyStep1Job(String name, SwtBusyHandler handler) {
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
        new SwtBusyStep2Job(getName(), getBusyHandler(), m_parts).schedule();
      }
    }
    return Status.OK_STATUS;
  }

  /**
   * Show a wait cursor until long operation timeout
   */
  @Override
  protected void runBusy(final IProgressMonitor monitor) {
    final IRunnableWithProgress busyWaitLoop = new IRunnableWithProgress() {
      @Override
      public void run(IProgressMonitor monitor2) throws InvocationTargetException, InterruptedException {
        SwtBusyStep1Job.super.runBusy(monitor2);
      }
    };
    final Display display = getBusyHandler().getDisplay();
    display.syncExec(new Runnable() {
      @Override
      public void run() {
        m_parts = findAffectedParts();
        BusyIndicator.showWhile(display, new Runnable() {
          @Override
          public void run() {
            try {
              ModalContext.run(busyWaitLoop, true, monitor, display);
            }
            catch (Throwable t) {
              LOG.warn("show busy", t);
            }
          }
        });
      }
    });
  }

  /**
   * @return all affected parts in the same swt / scout environment (user session). The first part is the active part
   *         and may be null.
   */
  protected List<ISwtScoutPart> findAffectedParts() {
    ArrayList<ISwtScoutPart> candidateParts = new ArrayList<ISwtScoutPart>();
    for (ISwtScoutPart part : getBusyHandler().getSwtEnvironment().getOpenFormParts()) {
      if (isDialogPart(part) || isViewOrEditorPart(part)) {
        candidateParts.add(part);
      }
    }
    ArrayList<ISwtScoutPart> affectedParts = new ArrayList<ISwtScoutPart>();
    //find an active dialog, it would be the only affected item
    for (ISwtScoutPart part : candidateParts) {
      if (part.isActive() && isDialogPart(part)) {
        affectedParts.add(part);
        return affectedParts;
      }
    }
    //find a visible dialog, it would be the only affected item
    for (ISwtScoutPart part : candidateParts) {
      if (part.isActive() && isDialogPart(part)) {
        affectedParts.add(part);
        return affectedParts;
      }
    }
    //find an active view, also all other views are affected
    for (ISwtScoutPart part : candidateParts) {
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

  public static boolean isDialogPart(ISwtScoutPart part) {
    return (part instanceof SwtScoutDialog);
  }

  public static boolean isViewOrEditorPart(ISwtScoutPart part) {
    return (part instanceof AbstractScoutView || part instanceof AbstractScoutEditorPart);
  }

}
