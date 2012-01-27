/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.workbench;

import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.core.util.AbstractRwtUtility;
import org.eclipse.scout.rt.ui.rap.workbench.util.listener.WindowListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

public class ScoutApplicationStartup {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(ScoutApplicationStartup.class);

  private final IRwtEnvironment m_uiEnvironment;

  public ScoutApplicationStartup(IRwtEnvironment uiEnvironment) {
    m_uiEnvironment = uiEnvironment;
  }

  public void startup() {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    workbench.getDisplay().asyncExec(new P_HandleInitWorkbench());
  }

  protected String getInitWorkbenchTaskText() {
    return AbstractRwtUtility.getNlsText(m_uiEnvironment.getDisplay(), "ScoutStarting");
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
      Subject subject = new Subject();
      subject.getPrincipals().add(new SimplePrincipal(System.getProperty("user.name")));
      Subject.doAs(subject, new PrivilegedAction<Object>() {
        @Override
        public Object run() {
          m_uiEnvironment.ensureInitialized();
          return null;
        }
      });
      monitor.done();
      return Status.OK_STATUS;
    }
  }
}
