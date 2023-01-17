/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ClientRunContextTest {

  @Test
  public void testEmpty() {
    ClientRunContext runContext = ClientRunContexts.empty();
    assertNull(runContext.getSubject());
    assertNull(runContext.getSession());
    assertNull(runContext.getUserAgent());
    assertNull(runContext.getLocale());
    assertEquals(TransactionScope.REQUIRED, runContext.getTransactionScope());
  }

  @Test
  public void testCopy() {
    ClientRunContext runContext = ClientRunContexts.empty();
    runContext.getPropertyMap().put("A", "B");
    runContext.withSubject(new Subject());
    runContext.withSession(mock(IClientSession.class), true);
    runContext.withUserAgent(UserAgents.create().build());
    runContext.withLocale(Locale.CANADA_FRENCH);

    ClientRunContext copy = runContext.copy();

    assertEquals(toSet(runContext.getPropertyMap().iterator()), toSet(copy.getPropertyMap().iterator()));
    assertSame(runContext.getSubject(), copy.getSubject());
    assertSame(runContext.getUserAgent(), copy.getUserAgent());
    assertSame(runContext.getLocale(), copy.getLocale());
    assertSame(runContext.getLocale(), copy.getLocale());
  }

  @Test
  public void testRunContextsEmpty() {
    RunContext runContext = RunContexts.empty();
    assertThat(runContext, CoreMatchers.instanceOf(ClientRunContext.class));
    ClientRunContext clientCtx = (ClientRunContext) runContext;
    assertNull(clientCtx.getSubject());
    assertNull(clientCtx.getSession());
    assertNull(clientCtx.getUserAgent());
    assertNull(clientCtx.getLocale());
    assertEquals(TransactionScope.REQUIRED, clientCtx.getTransactionScope());
  }

  @Test
  public void testRunContextsCopyCurrent() {
    final IClientSession session = mock(IClientSession.class);
    final UserAgent sessionUserAgent = UserAgents.create().build();
    final Locale sessionLocale = Locale.CANADA_FRENCH;
    final Subject sessionSubject = new Subject();
    final IDesktop sessionDesktop = mock(IDesktop.class);

    when(session.getUserAgent()).thenReturn(sessionUserAgent);
    when(session.getLocale()).thenReturn(sessionLocale);
    when(session.getSubject()).thenReturn(sessionSubject);
    when(session.getDesktopElseVirtualDesktop()).thenReturn(sessionDesktop);

    ClientRunContexts.empty().withSession(session, true).run(() -> {
      RunContext runContext = RunContexts.copyCurrent();
      assertThat(runContext, CoreMatchers.instanceOf(ClientRunContext.class));
      ClientRunContext clientCtx = (ClientRunContext) runContext;

      assertSame(session, clientCtx.getSession());
      assertSame(sessionLocale, clientCtx.getLocale());
      assertSame(sessionUserAgent, clientCtx.getUserAgent());
      assertSame(sessionDesktop, clientCtx.getDesktop());
    });
  }

  @Test
  public void testCurrentSessionAndDerivedValues() {
    final IClientSession session = mock(IClientSession.class);
    final UserAgent sessionUserAgent = UserAgents.create().build();
    final Locale sessionLocale = Locale.CANADA_FRENCH;
    final Subject sessionSubject = new Subject();
    final IDesktop sessionDesktop = mock(IDesktop.class);

    when(session.getUserAgent()).thenReturn(sessionUserAgent);
    when(session.getLocale()).thenReturn(sessionLocale);
    when(session.getSubject()).thenReturn(sessionSubject);
    when(session.getDesktopElseVirtualDesktop()).thenReturn(sessionDesktop);

    ClientRunContexts.empty().withSession(null, false).run(() -> {
      assertNull(ISession.CURRENT.get());
      assertNull(NlsLocale.CURRENT.get());
      assertNull(UserAgent.CURRENT.get());
      assertNull(IDesktop.CURRENT.get());

      assertNull(ClientRunContexts.copyCurrent().getSession());
      assertNull(ClientRunContexts.copyCurrent().getLocale());
      assertNull(ClientRunContexts.copyCurrent().getUserAgent());
      assertNull(ClientRunContexts.copyCurrent().getDesktop());
    });

    ClientRunContexts.empty().withSession(session, false).run(() -> {
      assertSame(session, ISession.CURRENT.get());
      assertNull(NlsLocale.CURRENT.get());
      assertNull(UserAgent.CURRENT.get());
      assertNull(IDesktop.CURRENT.get());

      assertSame(session, ClientRunContexts.copyCurrent().getSession());
      assertNull(ClientRunContexts.copyCurrent().getLocale());
      assertNull(ClientRunContexts.copyCurrent().getUserAgent());
      assertNull(ClientRunContexts.copyCurrent().getDesktop());

      final UserAgent customUserAgent = UserAgents.create().build();
      assertSame(customUserAgent, ClientRunContexts.copyCurrent().withUserAgent(customUserAgent).getUserAgent());
      final Locale customLocale = Locale.ITALIAN;
      assertSame(customLocale, ClientRunContexts.copyCurrent().withLocale(customLocale).getLocale());
    });

    ClientRunContexts.empty().withSession(session, true).run(() -> {
      assertSame(session, ISession.CURRENT.get());
      assertSame(sessionLocale, NlsLocale.CURRENT.get());
      assertSame(sessionUserAgent, UserAgent.CURRENT.get());
      assertSame(sessionDesktop, IDesktop.CURRENT.get());

      assertSame(session, ClientRunContexts.copyCurrent().getSession());
      assertSame(sessionLocale, ClientRunContexts.copyCurrent().getLocale());
      assertSame(sessionUserAgent, ClientRunContexts.copyCurrent().getUserAgent());
      assertSame(sessionDesktop, ClientRunContexts.copyCurrent().getDesktop());

      final UserAgent customUserAgent = UserAgents.create().build();
      assertSame(customUserAgent, ClientRunContexts.copyCurrent().withUserAgent(customUserAgent).getUserAgent());

      final Locale customLocale = Locale.ITALIAN;
      assertSame(customLocale, ClientRunContexts.copyCurrent().withLocale(customLocale).getLocale());

      final IDesktop customDesktop = mock(IDesktop.class);
      assertSame(customDesktop, ClientRunContexts.copyCurrent().withDesktop(customDesktop).getDesktop());
    });
  }

  @Test
  public void testCurrentUserAgent() {
    final UserAgent userAgent = UserAgents.create().build();
    ClientRunContexts.empty().withUserAgent(null).run(() -> {
      assertNull(UserAgent.CURRENT.get());
      assertNull(RunContexts.copyCurrent().getLocale());
    });

    ClientRunContexts.empty().withUserAgent(userAgent).run(() -> {
      assertSame(userAgent, UserAgent.CURRENT.get());
      assertEquals(userAgent, ClientRunContexts.copyCurrent().getUserAgent());

      final UserAgent customUserAgent = UserAgents.create().build();
      assertEquals(customUserAgent, ClientRunContexts.copyCurrent().withUserAgent(customUserAgent).getUserAgent());
    });
  }

  @Test
  public void testCurrentTransactionScope() {
    RunContexts.empty().run(() -> assertEquals(TransactionScope.REQUIRED, ClientRunContexts.copyCurrent().getTransactionScope()));
    RunContexts.empty().withTransactionScope(TransactionScope.REQUIRES_NEW).run(() -> assertEquals(TransactionScope.REQUIRED, ClientRunContexts.copyCurrent().getTransactionScope()));
  }

  @Test
  public void testCopyCurrentOrElseEmpty() {
    Jobs.schedule(() -> {
      try {
        ClientRunContexts.copyCurrent();
        fail("AssertionException expected because not running in a RunContext");
      }
      catch (AssertionException e) {
        // expected
      }

      try {
        ClientRunContexts.copyCurrent(false);
        fail("AssertionException expected because not running in a RunContext");
      }
      catch (AssertionException e) {
        // expected
      }

      assertNotNull(ClientRunContexts.copyCurrent(true));
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
