/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session.context;

import static org.junit.Assert.*;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.session.IServerSession;
import org.eclipse.scout.rt.shared.user.UserId;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ServerSessionRunContextProducerTest {

  @Test
  public void testProduceWithNoSession() {
    String username = "alpha";
    assertNull((((ServerSessionRunContext) RunContext.CURRENT.get()).getSession())); // ensure no previous session
    ServerSessionRunContextProducer producer = BEANS.get(ServerSessionRunContextProducer.class);
    ServerRunContext context = producer.produce(getSubjectForPrincipalName(username));
    assertEquals(username, context.call(UserId.CURRENT::get));
  }

  @Test
  public void testProduceWithSessionDifferentUsername() {
    String firstUsername = "alpha";
    runInRunContext(firstUsername, () -> {
      String secondUsername = "beta";
      IServerSession previousSession = ((ServerSessionRunContext) RunContext.CURRENT.get()).getSession();
      assertNotNull(previousSession); // ensure previous session
      ServerSessionRunContextProducer producer = BEANS.get(ServerSessionRunContextProducer.class);
      ServerSessionRunContext context = producer.produce(getSubjectForPrincipalName(secondUsername));
      IServerSession session = context.getSession();
      assertNotEquals(previousSession, session);
      assertEquals(secondUsername, context.call(UserId.CURRENT::get));
    });
  }

  @Test
  public void testProduceWithSessionSameUsername() {
    String firstUsername = "alpha";
    runInRunContext(firstUsername, () -> {
      String secondUsername = "alpha";
      IServerSession previousSession = ((ServerSessionRunContext) RunContext.CURRENT.get()).getSession();
      assertNotNull(previousSession); // ensure previous session
      ServerSessionRunContextProducer producer = BEANS.get(ServerSessionRunContextProducer.class);
      ServerSessionRunContext context = producer.produce(getSubjectForPrincipalName(secondUsername));
      IServerSession session = context.getSession();
      assertEquals(previousSession, session);
      assertEquals(secondUsername, context.call(UserId.CURRENT::get));
    });
  }

  protected void runInRunContext(String principalName, IRunnable runnable) {
    assertNull((((ServerSessionRunContext) RunContext.CURRENT.get()).getSession())); // ensure no previous session
    ServerSessionRunContextProducer producer = BEANS.get(ServerSessionRunContextProducer.class);
    ServerSessionRunContext context = producer.produce(getSubjectForPrincipalName(principalName));
    assertEquals(principalName, context.call(UserId.CURRENT::get));
    Jobs.schedule(runnable, Jobs.newInput().withRunContext(context)).awaitDoneAndGet();
  }

  protected Subject getSubjectForPrincipalName(String principalName) {
    Subject s = new Subject();
    s.getPrincipals().add(new SimplePrincipal(principalName));
    s.setReadOnly();
    return s;
  }
}
