/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.testenvironment;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.client.testenvironment.ui.desktop.TestEnvironmentDesktop;

/**
 * {@link IClientSession} for Client Test Environment
 * 
 * @author jbr
 */
public class TestEnvironmentClientSession extends AbstractClientSession {

  public TestEnvironmentClientSession() {
    super(true);
  }

  /**
   * @return session in current ThreadContext
   */
  public static TestEnvironmentClientSession get() {
    return ClientJob.getCurrentSession(TestEnvironmentClientSession.class);
  }

  @Override
  public void execLoadSession() throws ProcessingException {
    //do not enable client notifications, do not add service tunnel
    setDesktop(new TestEnvironmentDesktop());
  }

  @Override
  public void execStoreSession() throws ProcessingException {
  }

  /**
   * export method as public setter
   */
  @Override
  public void setServiceTunnel(IServiceTunnel tunnel) {
    super.setServiceTunnel(tunnel);
  }
}
