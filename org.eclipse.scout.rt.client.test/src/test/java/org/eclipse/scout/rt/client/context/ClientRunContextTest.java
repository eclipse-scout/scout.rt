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
package org.eclipse.scout.rt.client.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ClientRunContextTest {

  @Before
  public void before() {
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    ScoutTexts.CURRENT.remove();
    UserAgent.CURRENT.remove();
  }

  @After
  public void after() {
    ISession.CURRENT.remove();
  }

  @Test
  public void testEmpty() {
    ClientRunContext runContext = ClientRunContexts.empty();
    assertNull(runContext.getSubject());
    assertNull(runContext.getSession());
    assertNull(runContext.getUserAgent());
    assertNull(runContext.getLocale());
  }

  @Test
  public void testCopy() {
    ClientRunContext runContext = ClientRunContexts.empty();
    runContext.getPropertyMap().put("A", "B");
    runContext.withSubject(new Subject());
    runContext.withSession(mock(IClientSession.class), true);
    runContext.withUserAgent(UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a"));
    runContext.withLocale(Locale.CANADA_FRENCH);

    ClientRunContext copy = runContext.copy();

    assertEquals(toSet(runContext.getPropertyMap().iterator()), toSet(copy.getPropertyMap().iterator()));
    assertSame(runContext.getSubject(), copy.getSubject());
    assertSame(runContext.getUserAgent(), copy.getUserAgent());
    assertSame(runContext.getLocale(), copy.getLocale());
    assertSame(runContext.getLocale(), copy.getLocale());
  }

  @Test
  public void testCurrentSubject() {
    Subject subject = new Subject();
    ClientRunContext runContext = Subject.doAs(subject, new PrivilegedAction<ClientRunContext>() {

      @Override
      public ClientRunContext run() {
        return ClientRunContexts.copyCurrent();
      }
    });
    assertSame(subject, runContext.getSubject());

    runContext = Subject.doAs(null, new PrivilegedAction<ClientRunContext>() {

      @Override
      public ClientRunContext run() {
        return ClientRunContexts.copyCurrent();
      }
    });
    assertNull(runContext.getSubject());
  }

  @Test
  public void testCurrentSession() {
    // No session on ThreadLocal
    ISession.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().getSession());

    // Session on ThreadLocal
    IClientSession sessionThreadLocal = mock(IClientSession.class);
    ISession.CURRENT.set(sessionThreadLocal);
    assertSame(sessionThreadLocal, ClientRunContexts.copyCurrent().getSession());

    // Session on ThreadLocal, but set explicitly
    ISession.CURRENT.set(sessionThreadLocal);
    IClientSession explicitSession = mock(IClientSession.class);
    assertSame(explicitSession, ClientRunContexts.copyCurrent().withSession(explicitSession, true).getSession());

    // Session on ThreadLocal, but set explicitly to null
    ISession.CURRENT.set(sessionThreadLocal);
    assertNull(ClientRunContexts.copyCurrent().withSession(null, true).getSession());
  }

  @Test
  public void testCurrentLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ClientRunContexts.copyCurrent().getLocale());

    // ThreadLocal not set, Session set with Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().getLocale());
  }

  @Test
  public void testCurrentLocaleAndSetNullLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertSame(Locale.ITALY, ClientRunContexts.copyCurrent().withLocale(null).withSession(session, true).getLocale());
    assertNull(ClientRunContexts.copyCurrent().withLocale(null).withSession(session, false).getLocale());

    // ThreadLocal set, Session set with null Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientRunContexts.copyCurrent().withLocale(null).withSession(session, true).getLocale());
    assertNull(ClientRunContexts.copyCurrent().withLocale(null).withSession(session, false).getLocale());

    // ThreadLocal set, Session not set
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientRunContexts.copyCurrent().withLocale(null).withSession(session, true).getLocale());
    assertNull(ClientRunContexts.copyCurrent().withLocale(null).withSession(session, false).getLocale());

    // ThreadLocal not set, Session not set
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().withLocale(null).withSession(session, true).getLocale());
    assertNull(ClientRunContexts.copyCurrent().withLocale(null).withSession(session, false).getLocale());
  }

  @Test
  public void testCurrentLocaleAndSetNotNullLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.ITALY, ClientRunContexts.copyCurrent().withLocale(Locale.JAPAN).withSession(session, true).getLocale());
    assertEquals(Locale.JAPAN, ClientRunContexts.copyCurrent().withLocale(Locale.JAPAN).withSession(session, false).getLocale());

    // ThreadLocal set, Session set with null Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientRunContexts.copyCurrent().withLocale(Locale.JAPAN).withSession(session, true).getLocale());
    assertEquals(Locale.JAPAN, ClientRunContexts.copyCurrent().withLocale(Locale.JAPAN).withSession(session, false).getLocale());

    // ThreadLocal set, Session not set
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientRunContexts.copyCurrent().withLocale(Locale.JAPAN).withSession(session, true).getLocale());
    assertEquals(Locale.JAPAN, ClientRunContexts.copyCurrent().withLocale(Locale.JAPAN).withSession(session, false).getLocale());

    // ThreadLocal not set, Session not set
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().withLocale(Locale.JAPAN).withSession(session, true).getLocale());
    assertEquals(Locale.JAPAN, ClientRunContexts.copyCurrent().withLocale(Locale.JAPAN).withSession(session, false).getLocale());
  }

  @Test
  public void testCurrentUserAgent() {
    IClientSession session = mock(IClientSession.class);
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();

    // ThreadLocal set, Session set with UserAgent
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.set(userAgent2);
    assertSame(userAgent2, ClientRunContexts.copyCurrent().getUserAgent());

    // ThreadLocal not set, Session set with Locale
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().getUserAgent());
  }

  @Test
  public void testCurrentUserAgentAndSetNullUserAgent() {
    IClientSession session = mock(IClientSession.class);
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();

    // ThreadLocal set, Session set with UserAgent
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.set(userAgent2);
    assertSame(userAgent1, ClientRunContexts.copyCurrent().withUserAgent(null).withSession(session, true).getUserAgent());
    assertNull(ClientRunContexts.copyCurrent().withUserAgent(null).withSession(session, false).getUserAgent());

    // ThreadLocal set, Session set with null UserAgent
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(null);
    UserAgent.CURRENT.set(userAgent2);
    assertNull(ClientRunContexts.copyCurrent().withUserAgent(null).withSession(session, true).getUserAgent());
    assertNull(ClientRunContexts.copyCurrent().withUserAgent(null).withSession(session, false).getUserAgent());

    // ThreadLocal set, Session not set
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent2);
    assertNull(ClientRunContexts.copyCurrent().withUserAgent(null).withSession(session, true).getUserAgent());
    assertNull(ClientRunContexts.copyCurrent().withUserAgent(null).withSession(session, false).getUserAgent());

    // ThreadLocal not set, Session not set
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().withUserAgent(null).withSession(session, true).getUserAgent());
    assertNull(ClientRunContexts.copyCurrent().withUserAgent(null).withSession(session, false).getUserAgent());
  }

  @Test
  public void testCurrentUserAgentAndSetNotNullUserAgent() {
    IClientSession session = mock(IClientSession.class);
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();
    UserAgent userAgent3 = newUserAgent();

    // ThreadLocal set, Session set with UserAgent
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.set(userAgent2);
    assertEquals(userAgent3, ClientRunContexts.copyCurrent().withUserAgent(userAgent3).withSession(session, true).getUserAgent());

    // ThreadLocal set, Session set with null UserAgent
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(null);
    UserAgent.CURRENT.set(userAgent1);
    assertNull(ClientRunContexts.copyCurrent().withUserAgent(userAgent3).withSession(session, true).getUserAgent());
    assertEquals(userAgent3, ClientRunContexts.copyCurrent().withUserAgent(userAgent3).withSession(session, false).getUserAgent());

    // ThreadLocal set, Session not set
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertNull(ClientRunContexts.copyCurrent().withUserAgent(userAgent3).withSession(session, true).getUserAgent());
    assertEquals(userAgent3, ClientRunContexts.copyCurrent().withUserAgent(userAgent3).withSession(session, false).getUserAgent());

    // ThreadLocal not set, Session not set
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().withUserAgent(userAgent3).withSession(session, true).getUserAgent());
    assertEquals(userAgent3, ClientRunContexts.copyCurrent().withUserAgent(userAgent3).withSession(session, false).getUserAgent());
  }

  private static Set<Object> toSet(Iterator<?> iterator) {
    Set<Object> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }

  private static UserAgent newUserAgent() {
    return UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a");
  }
}
