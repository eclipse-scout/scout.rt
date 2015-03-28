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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
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
    runContext.session(mock(IClientSession.class));
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
    assertSame(explicitSession, ClientRunContexts.copyCurrent().session(explicitSession).session());

    // Session on ThreadLocal, but set explicitly to null
    ISession.CURRENT.set(sessionThreadLocal);
    assertNull(ClientRunContexts.copyCurrent().session(null).session());
  }

  @Test
  public void testCurrentLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale --> Locale from session
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.ITALY, ClientRunContexts.copyCurrent().locale());

    // ThreadLocal set, Session set with null Locale --> Null Locale from session
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientRunContexts.copyCurrent().locale());

    // ThreadLocal set, Session not set --> Locale from ThreadLocal
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ClientRunContexts.copyCurrent().locale());

    // ThreadLocal not set, Session not set --> no fallback to JVM default Locale.
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().locale());
  }

  @Test
  public void testCurrentLocaleAndSetNullLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale --> explicit Locale (null)
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientRunContexts.copyCurrent().locale(null).session(session).locale());

    // ThreadLocal set, Session set with null Locale --> explicit Locale (null)
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientRunContexts.copyCurrent().locale(null).session(session).locale());

    // ThreadLocal set, Session not set --> explicit Locale (null)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientRunContexts.copyCurrent().locale(null).session(session).locale());

    // ThreadLocal not set, Session not set --> explicit Locale (null)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().locale(null).session(session).locale());
  }

  @Test
  public void testCurrentLocaleAndSetNotNullLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale --> explicit Locale (JAPAN)
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.JAPAN, ClientRunContexts.copyCurrent().locale(Locale.JAPAN).session(session).locale());

    // ThreadLocal set, Session set with null Locale --> explicit Locale (JAPAN)
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.JAPAN, ClientRunContexts.copyCurrent().locale(Locale.JAPAN).session(session).locale());

    // ThreadLocal set, Session not set --> explicit Locale (JAPAN)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.JAPAN, ClientRunContexts.copyCurrent().locale(Locale.JAPAN).session(session).locale());

    // ThreadLocal not set, Session not set --> explicit Locale (JAPAN)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertEquals(Locale.JAPAN, ClientRunContexts.copyCurrent().locale(Locale.JAPAN).session(session).locale());
  }

  @Test
  public void testDerivedLocaleWhenSettingSession() {
    // Test with session ThreadLocal not set
    ISession.CURRENT.remove();
    testDerivedLocaleWhenSettingSessionImpl();

    // Test with session ThreadLocal set: expected same behavior because session is set explicitly
    IClientSession currentSession = mock(IClientSession.class);
    when(currentSession.getLocale()).thenReturn(Locale.ITALY);
    ISession.CURRENT.set(currentSession);
    testDerivedLocaleWhenSettingSessionImpl();
  }

  private void testDerivedLocaleWhenSettingSessionImpl() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Locale on session not set --> Null Locale
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    when(session.getLocale()).thenReturn(null);
    assertNull(ClientRunContexts.copyCurrent().session(session).locale());

    // ThreadLocal not set, Locale on session not set --> Null Locale
    NlsLocale.CURRENT.remove();
    when(session.getLocale()).thenReturn(null);
    assertNull(ClientRunContexts.copyCurrent().session(session).locale());

    // ThreadLocal not set, Locale on session set --> Locale from session
    NlsLocale.CURRENT.remove();
    when(session.getLocale()).thenReturn(Locale.CHINESE);
    assertEquals(Locale.CHINESE, ClientRunContexts.copyCurrent().session(session).locale());

    // ThreadLocal set, Locale on session set --> Locale from session
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    when(session.getLocale()).thenReturn(Locale.CHINESE);
    assertEquals(Locale.CHINESE, ClientRunContexts.copyCurrent().session(session).locale());
  }

  @Test
  public void testCurrentUserAgent() {
    IClientSession session = mock(IClientSession.class);
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();

    // ThreadLocal set, Session set with UserAgent --> UserAgent from session
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.set(userAgent2);
    assertSame(userAgent1, ClientRunContexts.copyCurrent().userAgent());

    // ThreadLocal set, Session set with null UserAgent --> Null UserAgent from session
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(null);
    UserAgent.CURRENT.set(userAgent1);
    assertNull(ClientRunContexts.copyCurrent().userAgent());

    // ThreadLocal set, Session not set --> UserAgent from ThreadLocal
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertSame(userAgent1, ClientRunContexts.copyCurrent().userAgent());

    // ThreadLocal not set, Session not set --> Null User Agent
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().userAgent());
  }

  @Test
  public void testCurrentUserAgentAndSetNullUserAgent() {
    IClientSession session = mock(IClientSession.class);
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();

    // ThreadLocal set, Session set with UserAgent --> explicit UserAgent (null)
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.set(userAgent2);
    assertNull(ClientRunContexts.copyCurrent().userAgent(null).session(session).userAgent());

    // ThreadLocal set, Session set with null UserAgent --> explicit UserAgent (null)
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(null);
    UserAgent.CURRENT.set(userAgent2);
    assertNull(ClientRunContexts.copyCurrent().userAgent(null).session(session).userAgent());

    // ThreadLocal set, Session not set --> explicit UserAgent (null)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent2);
    assertNull(ClientRunContexts.copyCurrent().userAgent(null).session(session).userAgent());

    // ThreadLocal not set, Session not set --> explicit UserAgent (null)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ClientRunContexts.copyCurrent().userAgent(null).session(session).userAgent());
  }

  @Test
  public void testCurrentUserAgentAndSetNotNullUserAgent() {
    IClientSession session = mock(IClientSession.class);
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();
    UserAgent userAgent3 = newUserAgent();

    // ThreadLocal set, Session set with UserAgent --> explicit UserAgent (JAPAN)
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.set(userAgent2);
    assertEquals(userAgent3, ClientRunContexts.copyCurrent().userAgent(userAgent3).session(session).userAgent());

    // ThreadLocal set, Session set with null UserAgent --> explicit UserAgent (JAPAN)
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(null);
    UserAgent.CURRENT.set(userAgent1);
    assertEquals(userAgent3, ClientRunContexts.copyCurrent().userAgent(userAgent3).session(session).userAgent());

    // ThreadLocal set, Session not set --> explicit UserAgent (JAPAN)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertEquals(userAgent3, ClientRunContexts.copyCurrent().userAgent(userAgent3).session(session).userAgent());

    // ThreadLocal not set, Session not set --> explicit UserAgent (JAPAN)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertEquals(userAgent3, ClientRunContexts.copyCurrent().userAgent(userAgent3).session(session).userAgent());
  }

  @Test
  public void testDerivedUserAgentWhenSettingSession() {
    // Test with session ThreadLocal not set
    ISession.CURRENT.remove();
    testDerivedUserAgentWhenSettingSessionImpl();

    // Test with session ThreadLocal set: expected same behavior because session is set explicitly
    IClientSession currentSession = mock(IClientSession.class);
    when(currentSession.getUserAgent()).thenReturn(newUserAgent());
    ISession.CURRENT.set(currentSession);
    testDerivedUserAgentWhenSettingSessionImpl();
  }

  private void testDerivedUserAgentWhenSettingSessionImpl() {
    IClientSession session = mock(IClientSession.class);
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();

    // ThreadLocal set, UserAgent on session not set --> Null UserAgent
    UserAgent.CURRENT.set(userAgent1);
    when(session.getUserAgent()).thenReturn(null);
    assertNull(ClientRunContexts.copyCurrent().session(session).userAgent());

    // ThreadLocal not set, UserAgent on session not set --> Null UserAgent
    UserAgent.CURRENT.remove();
    when(session.getUserAgent()).thenReturn(null);
    assertNull(ClientRunContexts.copyCurrent().session(session).userAgent());

    // ThreadLocal not set, UserAgent on session set --> UserAgent from session
    UserAgent.CURRENT.remove();
    when(session.getUserAgent()).thenReturn(userAgent1);
    assertEquals(userAgent1, ClientRunContexts.copyCurrent().session(session).userAgent());

    // ThreadLocal set, UserAgent on session set --> UserAgent from session
    UserAgent.CURRENT.set(userAgent1);
    when(session.getUserAgent()).thenReturn(userAgent2);
    assertEquals(userAgent2, ClientRunContexts.copyCurrent().session(session).userAgent());
  }

  @Test
  public void testDerivedSubjectWhenSettingSession() throws PrivilegedActionException {
    // Test with session ThreadLocal not set
    ISession.CURRENT.remove();
    testDerivedSubjectWhenSettingSessionImpl();

    // Test with session ThreadLocal set: expected same behavior because session is set explicitly
    IClientSession currentSession = mock(IClientSession.class);
    when(currentSession.getSubject()).thenReturn(new Subject());
    ISession.CURRENT.set(currentSession);
    testDerivedSubjectWhenSettingSessionImpl();
  }

  private void testDerivedSubjectWhenSettingSessionImpl() throws PrivilegedActionException {
    final IClientSession session = mock(IClientSession.class);
    final Subject subject1 = new Subject();
    final Subject subject2 = new Subject();

    // Current Subject set, Subject on session not set --> Null Subject
    Subject.doAs(subject1, new PrivilegedExceptionAction<Object>() {

      @Override
      public Object run() throws Exception {
        when(session.getSubject()).thenReturn(null);
        assertNull(ClientRunContexts.copyCurrent().session(session).subject());
        return null;
      }
    });

    // Current Subject not set, Subject on session not set --> Null Subject
    Subject.doAs(null, new PrivilegedExceptionAction<Object>() {

      @Override
      public Object run() throws Exception {
        when(session.getSubject()).thenReturn(null);
        assertNull(ClientRunContexts.copyCurrent().session(session).subject());
        return null;
      }
    });

    // Current Subject not set, Subject on session set --> Subject from session
    Subject.doAs(null, new PrivilegedExceptionAction<Object>() {

      @Override
      public Object run() throws Exception {
        when(session.getSubject()).thenReturn(subject1);
        assertSame(subject1, ClientRunContexts.copyCurrent().session(session).subject());
        return null;
      }
    });

    // Current Subject set, Subject on session set --> Subject from session
    Subject.doAs(subject1, new PrivilegedExceptionAction<Object>() {

      @Override
      public Object run() throws Exception {
        when(session.getSubject()).thenReturn(subject2);
        assertSame(subject2, ClientRunContexts.copyCurrent().session(session).subject());
        return null;
      }
    });
  }

  @Test
  public void testCurrentPropertyMap() {
    PropertyMap propertyMap = new PropertyMap();
    propertyMap.put("prop", "value");

    // No context on ThreadLocal
    PropertyMap.CURRENT.remove();
    assertNotNull(ClientRunContexts.copyCurrent());
    assertTrue(toSet(ClientRunContexts.copyCurrent().propertyMap().iterator()).isEmpty());

    // Context on ThreadLocal
    PropertyMap.CURRENT.set(propertyMap);
    assertNotSame(propertyMap, ClientRunContexts.copyCurrent().propertyMap()); // test for copy
    assertEquals(toSet(propertyMap.iterator()), toSet(ClientRunContexts.copyCurrent().propertyMap().iterator()));
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
