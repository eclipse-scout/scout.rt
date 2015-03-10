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
package org.eclipse.scout.testing.client.runner;

import java.util.List;

import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.services.common.exceptionhandler.WrappingProcessingRuntimeExceptionHandlerService;
import org.eclipse.scout.testing.client.servicetunnel.http.MultiClientAuthenticator;
import org.junit.runners.model.Statement;
import org.osgi.framework.FrameworkUtil;

/**
 * JUnit statements that runs the JUnit test within a Scout client session.
 */
public class ScoutClientJobWrapperStatement extends Statement {

  private final IClientSession m_clientSession;
  private final Statement m_statement;

  public ScoutClientJobWrapperStatement(IClientSession clientSession, Statement statement) {
    m_clientSession = clientSession;
    m_statement = statement;
  }

  @Override
  public void evaluate() throws Throwable {
    if (ISession.CURRENT.get() == m_clientSession) {
      doEvaluate();
    }
    else {
      ClientSyncJob job = new ClientSyncJob("JUnit Client Job Runner", m_clientSession) {
        @Override
        protected void runVoid(org.eclipse.core.runtime.IProgressMonitor monitor) throws Throwable {
          if (!m_clientSession.isLoaded()) {
            String userId = m_clientSession.getSubject().getPrincipals().iterator().next().getName();
            beforeStartSession(m_clientSession, userId);
            m_clientSession.startSession(Platform.isOsgiRunning() ? FrameworkUtil.getBundle(m_clientSession.getClass()) : null);
            simulateDesktopOpened(m_clientSession);
            afterStartSession(m_clientSession, userId);
          }
          doEvaluate();
        }
      };
      job.schedule();
      job.join();
      ScoutAssert.jobSuccessfullyCompleted(job);
    }
  }

  @Internal
  protected void doEvaluate() throws Throwable {
    List<IBean<?>> serviceReg = null;
    try {
      WrappingProcessingRuntimeExceptionHandlerService handler = new WrappingProcessingRuntimeExceptionHandlerService();
      serviceReg = TestingUtility.registerServices(1000, handler);
      m_statement.evaluate();
    }
    finally {
      if (serviceReg != null) {
        TestingUtility.unregisterServices(serviceReg);
      }
    }
  }

  /**
   * Performs custom operations before the client session is started. This default implementation assigns the current
   * session on the {@link MultiClientAuthenticator}, so that a possibly arising HTTP BASIC authentication can be
   * performed. Additionally, all message boxes are automatically canceled.
   *
   * @param clientSession
   * @param runAs
   * @see MultiClientAuthenticator
   */
  @Internal
  protected void beforeStartSession(IClientSession clientSession, String userId) {
    MultiClientAuthenticator.assignSessionToUser(clientSession, userId);
    TestingUtility.clearHttpAuthenticationCache();
    // auto-cancel all message boxes
    clientSession.getVirtualDesktop().addDesktopListener(new DesktopListener() {
      @Override
      public void desktopChanged(DesktopEvent e) {
        switch (e.getType()) {
          case DesktopEvent.TYPE_MESSAGE_BOX_ADDED:
            e.getMessageBox().getUIFacade().setResultFromUI(IMessageBox.CANCEL_OPTION);
            break;
        }
      }
    });
  }

  /**
   * Performs custom operations after the client session has been started.
   *
   * @param clientSession
   * @param runAs
   */
  @Internal
  protected void afterStartSession(IClientSession clientSession, String userId) {
  }

  /**
   * Simulates that the desktop has been opened. The method works also if the desktop has already been opened or if the
   * Scout client does not have a desktop at all.
   *
   * @param clientSession
   */
  @Internal
  protected void simulateDesktopOpened(IClientSession clientSession) {
    IDesktop desktop = clientSession.getDesktop();
    if (desktop instanceof AbstractDesktop && !desktop.isOpened()) {
      desktop.getUIFacade().fireGuiAttached();
      desktop.getUIFacade().fireDesktopOpenedFromUI();
    }
  }
}
