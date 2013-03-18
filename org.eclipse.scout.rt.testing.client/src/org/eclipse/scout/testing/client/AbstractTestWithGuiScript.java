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
package org.eclipse.scout.testing.client;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.outline.DefaultOutlineTableForm;
import org.eclipse.scout.rt.client.ui.form.outline.DefaultOutlineTreeForm;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.eclipse.scout.service.SERVICES;
import org.junit.Test;

/**
 * Subclasses of this type should NOT add the annotations {@link ClientSessionClass} but implement the methods
 * <ul>
 * <li>{@link #getSessionClass()}</li>
 * <li>{@link #runModel()}</li>
 * <li>{@link #runGui(IGuiMock, IClientSession)}</li>
 * </ul>
 */
public abstract class AbstractTestWithGuiScript {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractTestWithGuiScript.class);

  private boolean m_testActive;

  protected IClientSession clientSession;

  protected abstract Class<? extends IClientSession> getSessionClass();

  /**
   * Override this method
   * <p>
   * This method runs in the ui thread.
   */
  protected void runGui(IGuiMock gui) throws Throwable {
  }

  /**
   * Override this method
   * <p>
   * This method runs as the model "thread" using sync {@link org.eclipse.scout.rt.client.ClientRule}s
   */
  protected void runModel() throws Throwable {
  }

  /**
   * Override this method
   * <p>
   * This method runs as the model "thread" using sync {@link org.eclipse.scout.rt.client.ClientRule}s
   */
  protected void disposeModel() throws Throwable {
  }

  protected void resetSession() throws Throwable {
    IDesktop desktop = clientSession.getDesktop();
    desktop.setAvailableOutlines(null);
    desktop.setOutline((IOutline) null);
    for (IMessageBox m : desktop.getMessageBoxStack()) {
      try {
        m.getUIFacade().setResultFromUI(IMessageBox.CANCEL_OPTION);
      }
      catch (Throwable t) {
        LOG.warn("closing messagebox " + m.getClass(), t);
      }
    }
    for (IForm f : desktop.getDialogStack()) {
      try {
        f.doClose();
      }
      catch (Throwable t) {
        LOG.warn("closing dialog " + f.getClass(), t);
      }
    }
    for (IForm f : desktop.getViewStack()) {
      if (f instanceof DefaultOutlineTreeForm) {
        //leave it
      }
      else if (f instanceof DefaultOutlineTableForm) {
        //leave it
      }
      else {
        try {
          f.doClose();
        }
        catch (Throwable t) {
          LOG.warn("closing view " + f.getClass(), t);
        }
      }
    }
  }

  /**
   * This is the hardwired controller of the ui test.
   * <p>
   * First it schedules a new Job that calls {@link #runGui()}<br>
   * Then is calls {@link #runModel()} <br>
   * When the gui script has finished or failed it schedules back a model job that calls {@link #disposeModel()}
   * 
   * @throws Throwable
   */
  @Test
  public final void test() throws Throwable {
    IGuiMockService guiMockService = SERVICES.getService(IGuiMockService.class);
    if (guiMockService == null) {
      return;
    }
    clientSession = SERVICES.getService(IClientSessionRegistryService.class).newClientSession(getSessionClass(), guiMockService.initUserAgent());
    final IGuiMock gui = guiMockService.createMock(clientSession);
    gui.beforeTest();
    try {
      //
      final ClientSyncJob runModelJob = new ClientSyncJob("Run", clientSession) {
        @Override
        protected void runVoid(IProgressMonitor m) throws Throwable {
          resetSession();
          runModel();
        }
      };
      runModelJob.setUser(false);
      runModelJob.setSystem(true);
      //
      final ClientSyncJob disposeModelJob = new ClientSyncJob("Dispose", clientSession) {
        @Override
        protected void runVoid(IProgressMonitor m) throws Throwable {
          try {
            disposeModel();
          }
          finally {
            resetSession();
          }
        }
      };
      disposeModelJob.setUser(false);
      disposeModelJob.setSystem(true);
      //
      JobEx guiScriptJob = new JobEx("Gui Script") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
          try {
            gui.waitForIdle();
            runGui(gui);
            return Status.OK_STATUS;
          }
          catch (Throwable t) {
            return new Status(Status.WARNING, AbstractTestWithGuiScript.this.getClass().getName(), t.getMessage(), t);
          }
        }
      };
      guiScriptJob.setUser(false);
      guiScriptJob.setSystem(true);
      //
      try {
        m_testActive = true;
        runModelJob.schedule();
        while (!runModelJob.isWaitFor() && runModelJob.getState() != Job.NONE) {
          runModelJob.join(100);
        }
        guiScriptJob.schedule();
        guiScriptJob.join();
      }
      finally {
        m_testActive = false;
        disposeModelJob.schedule();
        disposeModelJob.join();
      }
      runModelJob.join();
      ScoutAssert.jobSuccessfullyCompleted(runModelJob);
      ScoutAssert.jobSuccessfullyCompleted(guiScriptJob);
      ScoutAssert.jobSuccessfullyCompleted(disposeModelJob);
    }
    finally {
      gui.afterTest();
    }
  }

  /**
   * Use this method inside {@link #runModel()} to check if the gui script (and the test) is still running or was
   * cancelled.
   */
  public boolean isTestActive() {
    return m_testActive;
  }

}
