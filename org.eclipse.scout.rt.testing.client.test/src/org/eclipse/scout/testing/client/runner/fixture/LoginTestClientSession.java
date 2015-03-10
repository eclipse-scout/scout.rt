/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client.runner.fixture;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;

/**
 *
 */
public class LoginTestClientSession extends TestEnvironmentClientSession {
  private static List<String> s_beforeStartRunAs;
  private static List<String> s_afterStartRunAs;

  @Override
  protected void execLoadSession() throws ProcessingException {
    super.execLoadSession();
  }

  @Override
  protected void beforeStartSession(IClientSession clientSession, String runAs) {
  }

  @Override
  protected void afterStartSession(IClientSession clientSession, String runAs) {
    s_afterStartRunAs.add(runAs);
  }

  public static void clearProtocol() {
    s_beforeStartRunAs = new ArrayList<String>();
    s_afterStartRunAs = new ArrayList<String>();
  }

  public static List<String> getBeforeStartRunAs() {
    return s_beforeStartRunAs;
  }

  public static List<String> getAfterStartRunAs() {
    return s_afterStartRunAs;
  }
}
