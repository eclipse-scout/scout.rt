/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.context;

import static org.junit.Assert.*;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ServerRunContextProducerTest {

  @Test
  public void testProduceWithNoSession() {
    String username = "alpha";
    assertNull((((ServerRunContext) RunContext.CURRENT.get()).getSession())); // ensure no previous session
    ServerRunContextProducer producer = BEANS.get(ServerRunContextProducer.class);
    ServerRunContext context = producer.produce(getSubjectForPrincipalName(username));
    IServerSession session = context.getSession();
    assertEquals(username, session.getUserId());
  }

  @Test
  public void testProduceWithSessionDifferentUsername() {
    String firstUsername = "alpha";
    runInRunContext(firstUsername, () -> {
      String secondUsername = "beta";
      IServerSession previousSession = ((ServerRunContext) RunContext.CURRENT.get()).getSession();
      assertNotNull(previousSession); // ensure previous session
      ServerRunContextProducer producer = BEANS.get(ServerRunContextProducer.class);
      ServerRunContext context = producer.produce(getSubjectForPrincipalName(secondUsername));
      IServerSession session = context.getSession();
      assertNotEquals(previousSession, session);
      assertEquals(secondUsername, session.getUserId());
    });
  }

  @Test
  public void testProduceWithSessionSameUsername() {
    String firstUsername = "alpha";
    runInRunContext(firstUsername, () -> {
      String secondUsername = "alpha";
      IServerSession previousSession = ((ServerRunContext) RunContext.CURRENT.get()).getSession();
      assertNotNull(previousSession); // ensure previous session
      ServerRunContextProducer producer = BEANS.get(ServerRunContextProducer.class);
      ServerRunContext context = producer.produce(getSubjectForPrincipalName(secondUsername));
      IServerSession session = context.getSession();
      assertEquals(previousSession, session);
      assertEquals(secondUsername, session.getUserId());
    });
  }

  protected void runInRunContext(String principalName, IRunnable runnable) {
    assertNull((((ServerRunContext) RunContext.CURRENT.get()).getSession())); // ensure no previous session
    ServerRunContextProducer producer = BEANS.get(ServerRunContextProducer.class);
    ServerRunContext context = producer.produce(getSubjectForPrincipalName(principalName));
    IServerSession session = context.getSession();
    assertEquals(principalName, session.getUserId());
    Jobs.schedule(runnable, Jobs.newInput().withRunContext(context)).awaitDoneAndGet();
  }

  protected Subject getSubjectForPrincipalName(String principalName) {
    Subject s = new Subject();
    s.getPrincipals().add(new SimplePrincipal(principalName));
    s.setReadOnly();
    return s;
  }
}
