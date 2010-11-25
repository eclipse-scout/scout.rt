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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.swt.util.listener.WindowListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

public abstract class AbstractSwtStartup implements IStartup {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSwtStartup.class);

  protected abstract ISwtEnvironment getSwtEnvironment();

  public void earlyStartup() {

    final IWorkbench workbench = PlatformUI.getWorkbench();
    workbench.getDisplay().asyncExec(new Runnable() {
      public void run() {
        boolean perspectiveOpend = false;
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow != null) {
          IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
          if (activePage != null) {
            if (activePage.getPerspective() != null) {
              perspectiveOpend = handlePerspectiveOpened(activePage.getPerspective().getId());
            }
          }
          if (!perspectiveOpend) {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new P_PerspectiveListener());
          }
        }
        // If the window is not active on startup no active workbenchWindow is
        // found
        else {
          PlatformUI.getWorkbench().addWindowListener(new WindowListener() {
            @Override
            public void windowActivated(IWorkbenchWindow window) {
              PlatformUI.getWorkbench().removeWindowListener(this);
              //
              PerspectiveAdapter listener = new P_PerspectiveListener();
              window.addPerspectiveListener(listener);
              // check if perspective is already open
              if (window.getActivePage() != null) {
                for (IPerspectiveDescriptor pd : window.getActivePage().getOpenPerspectives()) {
                  if (getSwtEnvironment().getPerspectiveId() != null && getSwtEnvironment().getPerspectiveId().equals(pd.getId())) {
                    listener.perspectiveOpened(window.getActivePage(), pd);
                    break;
                  }
                }
              }
            }
          });
        }
      }
    });
  }

  private class P_PerspectiveListener extends PerspectiveAdapter {
    @Override
    public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      // getSwtEnvironment().ensureInitialized();
      handlePerspectiveOpened(perspective.getId());
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().removePerspectiveListener(this);
    }

    @Override
    public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      // getSwtEnvironment().ensureInitialized();
      handlePerspectiveOpened(perspective.getId());
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().removePerspectiveListener(this);
    }
  }

  private synchronized boolean handlePerspectiveOpened(String perspectiveId) {
    // make sure that the desktop is only started once
    if (getSwtEnvironment().getPerspectiveId().equals(perspectiveId)) {
      final P_DesktopOpenedJob j = new P_DesktopOpenedJob(getDesktopOpenedTaskText());
      j.schedule(10);
      return true;
    }
    return false;
  }

  protected String getDesktopOpenedTaskText() {
    return ScoutTexts.get("ScoutStarting");
  }

  private final class P_DesktopOpenedJob extends UIJob {
    public P_DesktopOpenedJob(String name) {
      super(name);
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
      monitor.beginTask(getDesktopOpenedTaskText(), IProgressMonitor.UNKNOWN);
      getSwtEnvironment().ensureInitialized();
      monitor.done();
      return Status.OK_STATUS;
    }
  }
}
