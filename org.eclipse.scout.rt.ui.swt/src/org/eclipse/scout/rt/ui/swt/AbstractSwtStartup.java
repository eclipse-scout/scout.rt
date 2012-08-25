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
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.rt.ui.swt.util.listener.WindowListener;
import org.eclipse.swt.widgets.Display;
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

  @Override
  public void earlyStartup() {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    workbench.getDisplay().asyncExec(new P_HandleInitWorkbench());
  }

  private class P_HandleInitWorkbench implements Runnable {
    @Override
    public void run() {
      IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      if (activeWorkbenchWindow != null) {
        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        if (activePage == null || activePage.getPerspective() == null) {
          PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new P_PerspectiveListener());
        }
        else {
          handlePerspectiveOpened();
        }
      }

      //If the window is not active on startup no active workbenchWindow is found
      else {
        PlatformUI.getWorkbench().addWindowListener(new WindowListener() {
          @Override
          public void windowActivated(IWorkbenchWindow window) {
            PlatformUI.getWorkbench().removeWindowListener(this);
            //check if still no perspective open
            if (window.getActivePage() == null || window.getActivePage().getPerspective() == null) {
              window.addPerspectiveListener(new P_PerspectiveListener());
            }
            else {
              handlePerspectiveOpened();
            }
          }
        });
      }
    }
  }

  protected String getInitWorkbenchTaskText() {
    return SwtUtility.getNlsText(Display.getCurrent(), "ScoutStarting");
  }

  private synchronized void handlePerspectiveOpened() {
    final P_InitWorkbenchJob j = new P_InitWorkbenchJob(getInitWorkbenchTaskText());
    j.schedule(10);
  }

  //

  private class P_PerspectiveListener extends PerspectiveAdapter {
    @Override
    public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      page.getWorkbenchWindow().removePerspectiveListener(P_PerspectiveListener.this);

      handlePerspectiveOpened();
    }
  }

  private final class P_InitWorkbenchJob extends UIJob {
    public P_InitWorkbenchJob(String name) {
      super(name);
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
      monitor.beginTask(getInitWorkbenchTaskText(), IProgressMonitor.UNKNOWN);
      getSwtEnvironment().ensureInitialized();
      monitor.done();
      return Status.OK_STATUS;
    }
  }
}
