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
package org.eclipse.scout.rt.server.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ServerRunContextTest {

  @Test
  public void testEmpty() {
    ServerRunContext runContext = ServerRunContexts.empty();
    assertNull(runContext.getSubject());
    assertNull(runContext.getSession());
    assertNull(runContext.getUserAgent());
    assertNull(runContext.getLocale());
    assertEquals(TransactionScope.REQUIRES_NEW, runContext.getTransactionScope());
  }

  @Test
  public void testCopy() {
    ServerRunContext runContext = ServerRunContexts.empty();
    runContext.getPropertyMap().put("A", "B");
    runContext.withSubject(new Subject());
    runContext.withSession(mock(IServerSession.class));
    runContext.withUserAgent(UserAgents.create().build());
    runContext.withLocale(Locale.CANADA_FRENCH);
    runContext.withTransactionScope(TransactionScope.MANDATORY);

    ServerRunContext copy = runContext.copy();

    assertEquals(toSet(runContext.getPropertyMap().iterator()), toSet(copy.getPropertyMap().iterator()));
    assertSame(runContext.getSubject(), copy.getSubject());
    assertSame(runContext.getUserAgent(), copy.getUserAgent());
    assertSame(runContext.getLocale(), copy.getLocale());
    assertEquals(TransactionScope.MANDATORY, runContext.getTransactionScope());
  }

  @Test
  public void testRunContextsEmpty() {
    RunContext runContext = RunContexts.empty();
    assertThat(runContext, CoreMatchers.instanceOf(ServerRunContext.class));
    ServerRunContext serverCtx = (ServerRunContext) runContext;
    assertNull(serverCtx.getSubject());
    assertNull(serverCtx.getSession());
    assertNull(serverCtx.getUserAgent());
    assertNull(serverCtx.getLocale());
    assertEquals(TransactionScope.REQUIRES_NEW, serverCtx.getTransactionScope());
  }

  @Test
  public void testRunContextsCopyCurrent() {
    final IServerSession session = mock(IServerSession.class);
    final UserAgent userAgent = UserAgents.create().build();
    final Locale locale = Locale.CANADA_FRENCH;
    final Subject subject = new Subject();

    ServerRunContexts
        .empty()
        .withSession(session)
        .withUserAgent(userAgent)
        .withLocale(locale)
        .withSubject(subject)
        .run(new IRunnable() {
          @Override
          public void run() throws Exception {
            RunContext runContext = RunContexts.copyCurrent();
            assertThat(runContext, CoreMatchers.instanceOf(ServerRunContext.class));
            ServerRunContext serverCtx = (ServerRunContext) runContext;

            assertSame(session, serverCtx.getSession());
            assertSame(userAgent, serverCtx.getUserAgent());
            assertSame(locale, serverCtx.getLocale());
            assertSame(subject, serverCtx.getSubject());
          }
        });
  }

  @Test
  public void testCurrentTransactionScope() {
    RunContexts.empty().run(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertEquals(TransactionScope.REQUIRES_NEW, ServerRunContexts.copyCurrent().getTransactionScope());
      }
    });
    RunContexts.empty().withTransactionScope(TransactionScope.REQUIRED).run(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertEquals(TransactionScope.REQUIRES_NEW, ServerRunContexts.copyCurrent().getTransactionScope());
      }
    });
  }

  @Test
  public void testCopyCurrentOrElseEmpty() {
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          ServerRunContexts.copyCurrent();
          fail("AssertionException expected because not running in a RunContext");
        }
        catch (AssertionException e) {
          // expected
        }

        try {
          ServerRunContexts.copyCurrent(false);
          fail("AssertionException expected because not running in a RunContext");
        }
        catch (AssertionException e) {
          // expected
        }

        assertNotNull(ServerRunContexts.copyCurrent(true));
      }
    }, Jobs.newInput())
        .awaitDoneAndGet();
  }

  private static Set<Object> toSet(Iterator<?> iterator) {
    Set<Object> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }
}
