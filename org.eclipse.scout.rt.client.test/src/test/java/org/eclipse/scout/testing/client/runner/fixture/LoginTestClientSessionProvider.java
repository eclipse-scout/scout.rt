/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client.runner.fixture;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.testing.client.ClientSessionProviderWithCache;

public class LoginTestClientSessionProvider extends ClientSessionProviderWithCache {

  private static IClientSession s_currentSession;
  private static final List<String> s_beforeStartRunAs = new ArrayList<String>();
  private static final List<String> s_afterStartRunAs = new ArrayList<String>();

  public LoginTestClientSessionProvider() {
    clearProtocol();
  }

  @Override
  public <SESSION extends IClientSession> SESSION provide(ClientRunContext runContext) {
    SESSION clientSession = super.provide(runContext);
    s_currentSession = clientSession;
    return clientSession;
  }

  public static void clearProtocol() {
    s_beforeStartRunAs.clear();
    s_afterStartRunAs.clear();
    s_currentSession = null;
  }

  public static IClientSession getCurrentSession() {
    return s_currentSession;
  }

  public static List<String> getBeforeStartRunAs() {
    return s_beforeStartRunAs;
  }

  public static List<String> getAfterStartRunAs() {
    return s_afterStartRunAs;
  }

  public static class LoginTestClientSession extends TestEnvironmentClientSession {

    @Override
    public void start(String sessionId) {
      String runAs = Subject.getSubject(AccessController.getContext()).getPrincipals().iterator().next().getName();
      s_beforeStartRunAs.add(runAs);
      super.start(sessionId);
      s_afterStartRunAs.add(runAs);
    }
  }
}
