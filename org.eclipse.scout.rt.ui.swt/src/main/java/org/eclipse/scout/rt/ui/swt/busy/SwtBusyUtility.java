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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.busy.IBusyHandler;
import org.eclipse.scout.rt.client.busy.IBusyManagerService;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.scout.rt.ui.swt.window.desktop.editor.AbstractScoutEditorPart;
import org.eclipse.scout.rt.ui.swt.window.desktop.view.AbstractScoutView;
import org.eclipse.scout.rt.ui.swt.window.dialog.SwtScoutDialog;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Utilities to handle busy and blocking
 *
 * @author imo
 * @since 3.8
 */
public final class SwtBusyUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtBusyUtility.class);

  private SwtBusyUtility() {
  }

  /**
   * Must be called outside of the display thread.
   * <p>
   * Does NOT call {@link Display#syncExec(Runnable)} since this could lead to dead-locks.
   * <p>
   * Blocks and returns once the runnable has finished running.
   * <p>
   * Uses {@link BusyIndicator#showWhile(Display, Runnable)} and
   * {@link ModalContext#run(IRunnableWithProgress, boolean, IProgressMonitor, Display)}
   */
  public static void showBusyIndicator(final SwtBusyHandler busyHandler, final IRunnableWithProgress runnable, IProgressMonitor monitor) {
    if (!busyHandler.isEnabled()) {
      return;
    }

    final Object lock = new Object();

    final Display display = busyHandler.getDisplay();
    display.asyncExec(new Runnable() {
      @Override
      public void run() {
        if (!busyHandler.isEnabled()) {
          return;
        }

        try {
          BusyIndicator.showWhile(display, new Runnable() {
            @Override
            public void run() {
              try {
                //use modal context to prevent freezing of the gui
                ModalContext.run(new IRunnableWithProgress() {
                  @Override
                  public void run(IProgressMonitor monitor2) throws InvocationTargetException, InterruptedException {
                    runnable.run(monitor2);
                  }
                }, true, new NullProgressMonitor(), display);
              }
              catch (Throwable t) {
                LOG.warn("run modal context", t);
              }
            }
          });
        }
        finally {
          synchronized (lock) {
            lock.notifyAll();
          }
        }
      }
    });

    synchronized (lock) {
      try {
        lock.wait();
      }
      catch (InterruptedException e) {
        LOG.warn("Interrupted while waiting for the runnable providing busy feedback to complete.", e);
      }
    }
  }

  /**
   * Must be called outside of the display thread.
   * <p>
   * Does NOT call {@link Display#syncExec(Runnable)} since this could lead to dead-locks.
   * <p>
   * Blocks and returns once the runnable has finished running.
   */
  public static void showWorkbenchIndicator(final SwtBusyHandler busyHandler, final IRunnableWithProgress runnable) {
    if (!busyHandler.isEnabled()) {
      return;
    }

    final Object lock = new Object();

    final Display display = busyHandler.getDisplay();
    display.asyncExec(new Runnable() {
      @Override
      public void run() {
        if (!busyHandler.isEnabled()) {
          return;
        }

        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null) {
          return;
        }

        try {
          activeWorkbenchWindow.run(true, true, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor workbenchJobMonitor) throws InvocationTargetException, InterruptedException {
              runnable.run(workbenchJobMonitor);
            }
          });
        }
        catch (InvocationTargetException e) {
          LOG.warn("Exception while showing workbench busy indicator.", e);
        }
        catch (InterruptedException e) {
          LOG.warn("Exception while showing workbench busy indicator.", e);
        }
        finally {
          synchronized (lock) {
            lock.notifyAll();
          }
        }
      }
    });

    synchronized (lock) {
      try {
        lock.wait();
      }
      catch (InterruptedException e) {
        LOG.warn("Interrupted while waiting for the runnable blocking the workbench to complete.", e);
      }
    }
  }

  public static boolean isDialogPart(ISwtScoutPart part) {
    return (part instanceof SwtScoutDialog);
  }

  public static boolean isViewOrEditorPart(ISwtScoutPart part) {
    return (part instanceof AbstractScoutView || part instanceof AbstractScoutEditorPart);
  }

  /**
   * @return all affected parts in the same swt / scout environment (user session). The first part is the active part
   *         and may be null.
   */
  public static List<ISwtScoutPart> findAffectedParts(ISwtEnvironment env) {
    ArrayList<ISwtScoutPart> candidateParts = new ArrayList<ISwtScoutPart>();
    for (ISwtScoutPart part : env.getOpenFormParts()) {
      if (SwtBusyUtility.isDialogPart(part) || SwtBusyUtility.isViewOrEditorPart(part)) {
        candidateParts.add(part);
      }
    }
    ArrayList<ISwtScoutPart> affectedParts = new ArrayList<ISwtScoutPart>();
    //find an active dialog, it would be the only affected item
    for (ISwtScoutPart part : candidateParts) {
      if (part.isActive() && SwtBusyUtility.isDialogPart(part)) {
        affectedParts.add(part);
        return affectedParts;
      }
    }
    //find a visible dialog, it would be the only affected item
    for (ISwtScoutPart part : candidateParts) {
      if (part.isActive() && SwtBusyUtility.isDialogPart(part)) {
        affectedParts.add(part);
        return affectedParts;
      }
    }
    //find an active view, also all other views are affected
    for (ISwtScoutPart part : candidateParts) {
      if (part.isActive() && SwtBusyUtility.isViewOrEditorPart(part)) {
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

  /**
   * Runs the given {@link Runnable} once the {@link IWorkbench} is not blocked. If not being blocked at the time of the
   * method call, the {@link Runnable} is run immediately. Otherwise, it is run asynchronously in the UI-Thread once the
   * workbench is not blocked anymore.
   *
   * @param env
   *          {@link ISwtEnvironment}
   * @param runnable
   *          {@link Runnable} to be run.
   */
  public static void asyncIdleExec(final ISwtEnvironment env, final Runnable runnable) {
    final IBusyHandler busyHandler = SERVICES.getService(IBusyManagerService.class).getHandler(env.getClientSession());

    if (!busyHandler.isBlocking()) {
      runnable.run();
    }
    else {
      Job job = new Job("Wait for blocked workbench to be finish") {

        @Override
        protected IStatus run(IProgressMonitor monitor) {
          // Wait for the workbench to exit the blocking mode.
          busyHandler.waitForBlockingToEnd();

          // Execute the runnable in the UI-thread.
          if (!env.getDisplay().isDisposed()) {
            env.getDisplay().syncExec(runnable);
          }

          return Status.OK_STATUS;
        }
      };
      job.setSystem(true);
      job.setUser(false);
      job.schedule();
    }
  }
}
