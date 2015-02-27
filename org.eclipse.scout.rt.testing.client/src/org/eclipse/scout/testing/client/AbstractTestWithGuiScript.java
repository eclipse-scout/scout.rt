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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.commons.filter.NotFilter;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.filter.FutureFilter;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.job.IClientJobManager;
import org.eclipse.scout.rt.client.job.IModelJobManager;
import org.eclipse.scout.rt.client.job.filter.BlockedJobFilter;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.outline.DefaultOutlineTableForm;
import org.eclipse.scout.rt.client.ui.form.outline.DefaultOutlineTreeForm;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.service.SERVICES;
import org.junit.Assert;
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
  private PrintStream m_originalSystemErrStream = null;
  private ByteArrayOutputStream m_monitoringSystemErrStream = null;

  protected IClientSession m_clientSession;

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
    IDesktop desktop = m_clientSession.getDesktop();
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
   * Calls {@link #doTest()} and starts the test.
   * The timeout of the test is set to infinite but can be overridden by subclasses
   */
  @Test(timeout = 0)
  public void test() throws Throwable {
    doTest();
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
  public final void doTest() throws Throwable {
    IGuiMockService guiMockService = SERVICES.getService(IGuiMockService.class);
    if (guiMockService == null) {
      return;
    }
    m_clientSession = SERVICES.getService(IClientSessionRegistryService.class).newClientSession(getSessionClass(), null, guiMockService.initUserAgent());
    final IGuiMock gui = guiMockService.createMock(m_clientSession);
    gui.beforeTest();
    try {
      if (failWhenSystemErrIsNotEmpty()) {
        startMonitoringSystemErr();
      }

      IRunnable runModelJob = new IRunnable() {
        @Override
        public void run() throws Exception {
          try {
            resetSession();
            runModel();
          }
          catch (Throwable t) {
            throw new Exception(t);
          }
        }
      };

      IRunnable disposeModelJob = new IRunnable() {
        @Override
        public void run() throws Exception {
          try {
            try {
              disposeModel();
            }
            finally {
              resetSession();
            }
          }
          catch (Throwable t) {
            throw new Exception(t);
          }
        }
      };

      IRunnable guiScriptJob = new IRunnable() {
        @Override
        public void run() throws Exception {
          try {
            gui.waitForIdle();
            runGui(gui);
          }
          catch (Throwable t) {
            throw new Exception(t);
          }
        }
      };

      IModelJobManager modelJobManager = OBJ.one(IModelJobManager.class);
      IFuture<Void> runModelFuture = null;
      try {
        m_testActive = true;
        runModelFuture = modelJobManager.schedule(runModelJob, ClientJobInput.defaults().session(m_clientSession).name("Run"));
        modelJobManager.waitUntilDone(new AndFilter<IFuture<?>>(new FutureFilter(runModelFuture), new NotFilter<>(BlockedJobFilter.INSTANCE)), 1, TimeUnit.HOURS);
        IFuture<Void> guiScriptFuture = OBJ.one(IClientJobManager.class).schedule(guiScriptJob, ClientJobInput.defaults().session(m_clientSession).name("Gui Script"));
        guiScriptFuture.get();
      }
      finally {
        m_testActive = false;
        IFuture<Void> disposeFuture = modelJobManager.schedule(disposeModelJob, ClientJobInput.defaults().session(m_clientSession).name("Dispose"));
        disposeFuture.get();
      }
      runModelFuture.get();
    }
    finally {
      gui.afterTest();
      if (failWhenSystemErrIsNotEmpty()) {
        String errText = stopMonitoringSystemErr();
        Assert.assertTrue("System.err is not empty!: " + errText, StringUtility.isNullOrEmpty(errText));
      }
    }
  }

  /**
   * Redirects System.err for monitoring purpose
   *
   * @since 4.1.0
   */
  protected void startMonitoringSystemErr() {
    m_originalSystemErrStream = System.err;
    m_monitoringSystemErrStream = new ByteArrayOutputStream();
    System.setErr(new PrintStream(m_monitoringSystemErrStream, true));
  }

  /**
   * Stops the monitoring of System.err and sets the original System.err stream. {@link #startMonitoringSystemErr()}
   * needs to be called before.
   * The method returns the content of System.err
   *
   * @since 4.1.0
   */
  protected String stopMonitoringSystemErr() {
    System.setErr(m_originalSystemErrStream);
    String errText = new String(m_monitoringSystemErrStream.toByteArray());
    m_monitoringSystemErrStream = null;
    return errText;
  }

  /**
   * Use this method inside {@link #runModel()} to check if the gui script (and the test) is still running or was
   * cancelled.
   */
  public boolean isTestActive() {
    return m_testActive;
  }

  /**
   * Configures whether System.err should be monitored or not.
   * If it is set to <code>true</code> the test will fail if System.err contains some content.
   * If it is set to <code>false</code> System.err is not monitored and ignored.
   * Override this method in your test if you want to have monitoring.
   */
  protected boolean failWhenSystemErrIsNotEmpty() {
    return false;
  }

}
