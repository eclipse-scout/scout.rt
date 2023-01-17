/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.testenvironment;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.ui.desktop.TestEnvironmentDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * {@link IClientSession} for Client Test Environment
 *
 * @author jbr
 */
@IgnoreBean
public class TestEnvironmentClientSession extends AbstractClientSession {

  private IDesktop m_testDesktop;

  public TestEnvironmentClientSession() {
    super(true);
  }

  /**
   * @return session in current ThreadContext
   */
  public static TestEnvironmentClientSession get() {
    return ClientSessionProvider.currentSession(TestEnvironmentClientSession.class);
  }

  @Override
  protected void execLoadSession() {
    // do not enable client notifications, do not add service tunnel
    final TestEnvironmentDesktop d = new TestEnvironmentDesktop();
    setDesktop(d);
    simulateDesktopOpened(d);
  }

  @Override
  public String getUserId() {
    String userId = super.getUserId();
    if (StringUtility.isNullOrEmpty(userId)) {
      userId = System.getProperty("user.name");
    }
    return userId;
  }

  @Override
  public IDesktop getDesktop() {
    if (m_testDesktop == null) {
      return super.getDesktop();
    }
    else {
      return m_testDesktop;
    }
  }

  /**
   * Replace the desktop with an other instance ({@link #m_testDesktop}). Can be used to install a mock or a spy. Unlike
   * {@link #setDesktop(IDesktop)} it will not check if the desktop is already active. If a test desktop (
   * {@link #m_testDesktop}) is set, {@link #getDesktop()} will return this instance. Do not forget to set the test
   * desktop to null at the end of your test.
   *
   * @param desktop
   *          the test desktop
   */
  public void replaceDesktop(IDesktop desktop) {
    m_testDesktop = desktop;
  }

  /**
   * Simulates that the desktop has been opened.
   */
  protected void simulateDesktopOpened(IDesktop desktop) {
    desktop.getUIFacade().openFromUI();
    desktop.getUIFacade().fireGuiAttached();
  }
}
