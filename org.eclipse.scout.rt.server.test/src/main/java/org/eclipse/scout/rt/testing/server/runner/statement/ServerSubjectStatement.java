/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.server.runner.statement;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.security.User;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.shared.session.SessionId;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.runner.statement.SubjectStatement;
import org.junit.runners.model.Statement;

/**
 * Statement to execute the following statements under a particular user given by a {@link Subject}.
 * <p>
 * Additionally sets the {@link User} for the given subject, defines a
 * random {@link SessionId} and sets the default user agent.
 *
 * @see RunWithSubject
 */
public class ServerSubjectStatement extends SubjectStatement {

  public ServerSubjectStatement(final Statement next, final RunWithSubject annotation) {
    super(next, annotation);
  }

  @Override
  protected RunContext createRunContext() {
    ServerRunContext context = ((ServerRunContext) super.createRunContext())
        .withThreadLocal(SessionId.CURRENT, SessionId.randomSessionId()) // set random session id so that server tests trying to access session id can run without a client session
        .withUserAgent(UserAgents.createDefault());
    return initRunContext(context);
  }

  @Override
  protected User createUser() {
    return BEANS.get(IAccessControlService.class).getUser(getSubject());
  }

  protected ServerRunContext initRunContext(ServerRunContext runContext) {
    return runContext;
  }
}
