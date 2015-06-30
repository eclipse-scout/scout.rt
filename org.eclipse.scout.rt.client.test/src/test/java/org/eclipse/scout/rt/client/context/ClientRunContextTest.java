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

import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
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
    assertNull(runContext.subject());
    assertNull(runContext.session());
    assertNull(runContext.userAgent());
    assertNull(runContext.locale());
  }

  @Test
  public void testCopy() {
    ClientRunContext runContext = ClientRunContexts.empty();
    runContext.propertyMap().put("A", "B");
    runContext.subject(new Subject());
    runContext.session(mock(IClientSession.class), true);
    runContext.userAgent(UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a"));
    runContext.locale(Locale.CANADA_FRENCH);

    ClientRunContext copy = runContext.copy();

    assertEquals(toSet(runContext.propertyMap().iterator()), toSet(copy.propertyMap().iterator()));
    assertSame(runContext.subject(), copy.subject());
    assertSame(runContext.userAgent(), copy.userAgent());
    assertSame(runContext.locale(), copy.locale());
    assertSame(runContext.locale(), copy.locale());
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
    assertSame(subject, runContext.subject());

    runContext = Subject.doAs(null, new PrivilegedAction<ClientRunContext>() {

      @Override
      public ClientRunContext run() {
        return ClientRunContexts.copyCurrent();
      }
    });
    assertNull(runContext.subject());
  }

  @Test
  public void testCurrentSession() {
    // No session on ThreadLocal
    ISession.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().session());

    // Session on ThreadLocal
    IClientSession sessionThreadLocal = mock(IClientSession.class);
    ISession.CURRENT.set(sessionThreadLocal);
    assertSame(sessionThreadLocal, ClientRunContexts.copyCurrent().session());

    // Session on ThreadLocal, but set explicitly
    ISession.CURRENT.set(sessionThreadLocal);
    IClientSession explicitSession = mock(IClientSession.class);
    assertSame(explicitSession, ClientRunContexts.copyCurrent().session(explicitSession, true).session());

    // Session on ThreadLocal, but set explicitly to null
    ISession.CURRENT.set(sessionThreadLocal);
    assertNull(ClientRunContexts.copyCurrent().session(null, true).session());
  }

  @Test
  public void testCurrentLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ClientRunContexts.copyCurrent().locale());

    // ThreadLocal not set, Session set with Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().locale());
  }

  @Test
  public void testCurrentLocaleAndSetNullLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertSame(Locale.ITALY, ClientRunContexts.copyCurrent().locale(null).session(session, true).locale());
    assertNull(ClientRunContexts.copyCurrent().locale(null).session(session, false).locale());

    // ThreadLocal set, Session set with null Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientRunContexts.copyCurrent().locale(null).session(session, true).locale());
    assertNull(ClientRunContexts.copyCurrent().locale(null).session(session, false).locale());

    // ThreadLocal set, Session not set
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientRunContexts.copyCurrent().locale(null).session(session, true).locale());
    assertNull(ClientRunContexts.copyCurrent().locale(null).session(session, false).locale());

    // ThreadLocal not set, Session not set
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().locale(null).session(session, true).locale());
    assertNull(ClientRunContexts.copyCurrent().locale(null).session(session, false).locale());
  }

  @Test
  public void testCurrentLocaleAndSetNotNullLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.ITALY, ClientRunContexts.copyCurrent().locale(Locale.JAPAN).session(session, true).locale());
    assertEquals(Locale.JAPAN, ClientRunContexts.copyCurrent().locale(Locale.JAPAN).session(session, false).locale());

    // ThreadLocal set, Session set with null Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientRunContexts.copyCurrent().locale(Locale.JAPAN).session(session, true).locale());
    assertEquals(Locale.JAPAN, ClientRunContexts.copyCurrent().locale(Locale.JAPAN).session(session, false).locale());

    // ThreadLocal set, Session not set
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientRunContexts.copyCurrent().locale(Locale.JAPAN).session(session, true).locale());
    assertEquals(Locale.JAPAN, ClientRunContexts.copyCurrent().locale(Locale.JAPAN).session(session, false).locale());

    // ThreadLocal not set, Session not set
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().locale(Locale.JAPAN).session(session, true).locale());
    assertEquals(Locale.JAPAN, ClientRunContexts.copyCurrent().locale(Locale.JAPAN).session(session, false).locale());
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
    assertSame(userAgent2, ClientRunContexts.copyCurrent().userAgent());

    // ThreadLocal not set, Session set with Locale
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().userAgent());
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
    assertSame(userAgent1, ClientRunContexts.copyCurrent().userAgent(null).session(session, true).userAgent());
    assertNull(ClientRunContexts.copyCurrent().userAgent(null).session(session, false).userAgent());

    // ThreadLocal set, Session set with null UserAgent
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(null);
    UserAgent.CURRENT.set(userAgent2);
    assertNull(ClientRunContexts.copyCurrent().userAgent(null).session(session, true).userAgent());
    assertNull(ClientRunContexts.copyCurrent().userAgent(null).session(session, false).userAgent());

    // ThreadLocal set, Session not set
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent2);
    assertNull(ClientRunContexts.copyCurrent().userAgent(null).session(session, true).userAgent());
    assertNull(ClientRunContexts.copyCurrent().userAgent(null).session(session, false).userAgent());

    // ThreadLocal not set, Session not set
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().userAgent(null).session(session, true).userAgent());
    assertNull(ClientRunContexts.copyCurrent().userAgent(null).session(session, false).userAgent());
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
    assertEquals(userAgent3, ClientRunContexts.copyCurrent().userAgent(userAgent3).session(session, true).userAgent());

    // ThreadLocal set, Session set with null UserAgent
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(null);
    UserAgent.CURRENT.set(userAgent1);
    assertNull(ClientRunContexts.copyCurrent().userAgent(userAgent3).session(session, true).userAgent());
    assertEquals(userAgent3, ClientRunContexts.copyCurrent().userAgent(userAgent3).session(session, false).userAgent());

    // ThreadLocal set, Session not set
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertNull(ClientRunContexts.copyCurrent().userAgent(userAgent3).session(session, true).userAgent());
    assertEquals(userAgent3, ClientRunContexts.copyCurrent().userAgent(userAgent3).session(session, false).userAgent());

    // ThreadLocal not set, Session not set
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().userAgent(userAgent3).session(session, true).userAgent());
    assertEquals(userAgent3, ClientRunContexts.copyCurrent().userAgent(userAgent3).session(session, false).userAgent());
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
