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
package org.eclipse.scout.testing.client.runner.fixture;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.session.ClientSessionProviderWithCache;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.Bean;

@Bean
@Priority(10)
public class LoginTestClientSessionProvider extends ClientSessionProviderWithCache {

  private static IClientSession s_currentSession;
  private static final List<String> s_beforeStartRunAs = new ArrayList<String>();
  private static final List<String> s_afterStartRunAs = new ArrayList<String>();

  public LoginTestClientSessionProvider() {
    clearProtocol();
  }

  @Override
  public <T extends IClientSession> T provide(ClientJobInput input) throws ProcessingException {
    T clientSession = super.provide(input);
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

  @Bean
  @Priority(10)
  public static class LoginTestClientSession extends TestEnvironmentClientSession {

    @Override
    public void startSession() {
      String runAs = Subject.getSubject(AccessController.getContext()).getPrincipals().iterator().next().getName();
      s_beforeStartRunAs.add(runAs);
      super.startSession();
      s_afterStartRunAs.add(runAs);
    }
  }
}
