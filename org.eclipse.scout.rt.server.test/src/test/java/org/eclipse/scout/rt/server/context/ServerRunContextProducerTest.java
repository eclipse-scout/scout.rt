/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.context;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PlatformTestRunner.class)
public class ServerRunContextProducerTest {

  @Test
  public void testProduceWithNoSession() {
    String username = "alpha";
    ServerRunContextProducer producer = BEANS.get(ServerRunContextProducer.class);
    ServerRunContext context = producer.produce(getSubjectForPrincipalName(username));
    IServerSession session = context.getSession();
    assertEquals(username, session.getUserId());
  }

  @Test
  public void testProduceWithSessionDifferentUsername() {
    String firstUsername = "alpha";
    final ServerRunContextProducer producer = BEANS.get(ServerRunContextProducer.class);
    ServerRunContext outerRunContext = producer.produce(getSubjectForPrincipalName(firstUsername));
    final IServerSession outerSession = outerRunContext.getSession();
    assertNotNull(outerSession); // ensure previous session
    assertEquals(firstUsername, outerSession.getUserId());
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        String secondUsername = "beta";
        ServerRunContext context = producer.produce(getSubjectForPrincipalName(secondUsername));
        IServerSession session = context.getSession();
        assertNotEquals(outerSession, session);
        assertEquals(secondUsername, session.getUserId());
      }
    }, Jobs.newInput().withRunContext(outerRunContext)).awaitDoneAndGet();
  }

  @Test
  public void testProduceWithSessionSameUsername() {
    String firstUsername = "alpha";
    final ServerRunContextProducer producer = BEANS.get(ServerRunContextProducer.class);
    ServerRunContext outerRunContext = producer.produce(getSubjectForPrincipalName(firstUsername));
    final IServerSession outerSession = outerRunContext.getSession();
    assertNotNull(outerSession); // ensure previous session
    assertEquals(firstUsername, outerSession.getUserId());
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        String secondUsername = "alpha";
        ServerRunContext context = producer.produce(getSubjectForPrincipalName(secondUsername));
        IServerSession session = context.getSession();
        assertEquals(outerSession, session);
        assertEquals(secondUsername, session.getUserId());
      }
    }, Jobs.newInput().withRunContext(outerRunContext)).awaitDoneAndGet();
  }

  protected Subject getSubjectForPrincipalName(String principalName) {
    Subject s = new Subject();
    s.getPrincipals().add(new SimplePrincipal(principalName));
    s.setReadOnly();
    return s;
  }
}
