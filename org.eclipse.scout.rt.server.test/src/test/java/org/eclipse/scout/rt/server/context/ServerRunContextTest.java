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
package org.eclipse.scout.rt.server.context;

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
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.transaction.TransactionScope;
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
public class ServerRunContextTest {

  @Before
  public void before() {
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    ScoutTexts.CURRENT.remove();
    UserAgent.CURRENT.remove();
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.remove();
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.remove();
  }

  @Test
  public void testEmpty() {
    ServerRunContext runContext = ServerRunContexts.empty();
    assertNull(runContext.subject());
    assertNull(runContext.session());
    assertNull(runContext.userAgent());
    assertNull(runContext.locale());
    assertEquals(TransactionScope.REQUIRES_NEW, runContext.transactionScope());
  }

  @Test
  public void testCopy() {
    ServerRunContext runContext = ServerRunContexts.empty();
    runContext.propertyMap().put("A", "B");
    runContext.subject(new Subject());
    runContext.session(mock(IServerSession.class));
    runContext.userAgent(UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a"));
    runContext.locale(Locale.CANADA_FRENCH);
    runContext.transactionScope(TransactionScope.MANDATORY);

    ServerRunContext copy = runContext.copy();

    assertEquals(toSet(runContext.propertyMap().iterator()), toSet(copy.propertyMap().iterator()));
    assertSame(runContext.subject(), copy.subject());
    assertSame(runContext.userAgent(), copy.userAgent());
    assertSame(runContext.locale(), copy.locale());
    assertSame(runContext.locale(), copy.locale());
    assertEquals(TransactionScope.MANDATORY, runContext.transactionScope());
  }

  @Test
  public void testCurrentSubject() {
    Subject subject = new Subject();
    ServerRunContext runContext = Subject.doAs(subject, new PrivilegedAction<ServerRunContext>() {

      @Override
      public ServerRunContext run() {
        return ServerRunContexts.copyCurrent();
      }
    });
    assertSame(subject, runContext.subject());

    runContext = Subject.doAs(null, new PrivilegedAction<ServerRunContext>() {

      @Override
      public ServerRunContext run() {
        return ServerRunContexts.copyCurrent();
      }
    });
    assertNull(runContext.subject());
  }

  @Test
  public void testCurrentSession() {
    // No session on ThreadLocal
    ISession.CURRENT.remove();
    assertNull(ServerRunContexts.copyCurrent().session());

    // Session on ThreadLocal
    IServerSession sessionThreadLocal = mock(IServerSession.class);
    ISession.CURRENT.set(sessionThreadLocal);
    assertSame(sessionThreadLocal, ServerRunContexts.copyCurrent().session());

    // Session on ThreadLocal, but set explicitly
    ISession.CURRENT.set(sessionThreadLocal);
    IServerSession explicitSession = mock(IServerSession.class);
    assertSame(explicitSession, ServerRunContexts.copyCurrent().session(explicitSession).session());

    // Session on ThreadLocal, but set explicitly to null
    ISession.CURRENT.set(sessionThreadLocal);
    assertNull(ServerRunContexts.copyCurrent().session(null).session());
  }

  @Test
  public void testCurrentLocale() {
    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set, Session available --> Locale from ThreadLocal
    ISession.CURRENT.set(session);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ServerRunContexts.copyCurrent().locale());

    // ThreadLocal set, Session available --> Locale from ThreadLocal
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ServerRunContexts.copyCurrent().locale());

    // ThreadLocal not set, Session not available --> no fallback to JVM default Locale.
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ServerRunContexts.copyCurrent().locale());
  }

  @Test
  public void testCurrentLocaleAndSetNullLocale() {
    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set, Session available --> explicit Locale (null)
    ISession.CURRENT.set(session);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ServerRunContexts.copyCurrent().locale(null).session(session).locale());

    // ThreadLocal set, Session not available --> explicit Locale (null)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ServerRunContexts.copyCurrent().locale(null).session(session).locale());

    // ThreadLocal not set, Session not available --> explicit Locale (null)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ServerRunContexts.copyCurrent().locale(null).session(session).locale());
  }

  @Test
  public void testCurrentLocaleAndSetNotNullLocale() {
    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set, Session available --> explicit Locale (JAPAN)
    ISession.CURRENT.set(session);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.JAPAN, ServerRunContexts.copyCurrent().locale(Locale.JAPAN).session(session).locale());

    // ThreadLocal set, Session not available --> explicit Locale (JAPAN)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.JAPAN, ServerRunContexts.copyCurrent().locale(Locale.JAPAN).session(session).locale());

    // ThreadLocal not set, Session not available --> explicit Locale (JAPAN)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertEquals(Locale.JAPAN, ServerRunContexts.copyCurrent().locale(Locale.JAPAN).session(session).locale());
  }

  @Test
  public void testDerivedLocaleWhenSettingSession() {
    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set --> Locale form ThreadLocal
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ServerRunContexts.copyCurrent().session(session).locale());

    // ThreadLocal not set --> Null Locale
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ServerRunContexts.copyCurrent().session(session).locale());

    IServerSession currentSession = mock(IServerSession.class);

    // ThreadLocal-Session available, ThreadLocal set --> Locale from ThreadLocal
    ISession.CURRENT.set(currentSession);
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ServerRunContexts.copyCurrent().session(session).locale());

    // ThreadLocal-Session available, ThreadLocal not set --> Null Locale
    ISession.CURRENT.set(currentSession);
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ServerRunContexts.copyCurrent().session(session).locale());
  }

  @Test
  public void testCurrentUserAgent() {
    UserAgent userAgent = newUserAgent();

    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set, Session available --> UserAgent from ThreadLocal
    ISession.CURRENT.set(session);
    UserAgent.CURRENT.set(userAgent);
    assertSame(userAgent, ServerRunContexts.copyCurrent().userAgent());

    // ThreadLocal set, Session available --> UserAgent from ThreadLocal
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent);
    assertEquals(userAgent, ServerRunContexts.copyCurrent().userAgent());

    // ThreadLocal not set, Session not available
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ServerRunContexts.copyCurrent().userAgent());
  }

  @Test
  public void testCurrentUserAgentAndSetNullUserAgent() {
    UserAgent userAgent = newUserAgent();
    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set, Session available --> explicit UserAgent (null)
    ISession.CURRENT.set(session);
    UserAgent.CURRENT.set(userAgent);
    assertNull(ServerRunContexts.copyCurrent().userAgent(null).session(session).userAgent());

    // ThreadLocal set, Session not available --> explicit UserAgent (null)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent);
    assertNull(ServerRunContexts.copyCurrent().userAgent(null).session(session).userAgent());

    // ThreadLocal not set, Session not available --> explicit UserAgent (null)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ServerRunContexts.copyCurrent().userAgent(null).session(session).userAgent());
  }

  @Test
  public void testCurrentUserAgentAndSetNotNullUserAgent() {
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();

    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set, Session available --> explicit UserAgent (userAgent2)
    ISession.CURRENT.set(session);
    UserAgent.CURRENT.set(userAgent1);
    assertSame(userAgent2, ServerRunContexts.copyCurrent().userAgent(userAgent2).session(session).userAgent());

    // ThreadLocal set, Session not available --> explicit UserAgent (userAgent2)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertSame(userAgent2, ServerRunContexts.copyCurrent().userAgent(userAgent2).session(session).userAgent());

    // ThreadLocal not set, Session not available --> explicit UserAgent (userAgent2)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertSame(userAgent2, ServerRunContexts.copyCurrent().userAgent(userAgent2).session(session).userAgent());
  }

  @Test
  public void testDerivedUserAgentWhenSettingSession() {
    UserAgent userAgent1 = newUserAgent();

    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set --> UserAgent form ThreadLocal
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertSame(userAgent1, ServerRunContexts.copyCurrent().session(session).userAgent());

    // ThreadLocal not set --> Null UserAgent
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ServerRunContexts.copyCurrent().session(session).userAgent());

    IServerSession currentSession = mock(IServerSession.class);

    // ThreadLocal-Session available, ThreadLocal set --> UserAgent from ThreadLocal
    ISession.CURRENT.set(currentSession);
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertSame(userAgent1, ServerRunContexts.copyCurrent().session(session).userAgent());

    // ThreadLocal-Session available, ThreadLocal not set --> Null UserAgent
    ISession.CURRENT.set(currentSession);
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ServerRunContexts.copyCurrent().session(session).userAgent());
  }

  @Test
  public void testDerivedSubjectWhenSettingSession() throws PrivilegedActionException {
    // Test with session ThreadLocal not set
    ISession.CURRENT.remove();
    testDerivedSubjectWhenSettingSessionImpl();

    // Test with session ThreadLocal set: expected same behavior because session is set explicitly
    IServerSession currentSession = mock(IServerSession.class);
    when(currentSession.getSubject()).thenReturn(new Subject());
    ISession.CURRENT.set(currentSession);
    testDerivedSubjectWhenSettingSessionImpl();
  }

  @Test
  public void testCurrentTransactionScope() {
    assertEquals(TransactionScope.REQUIRES_NEW, ServerRunContexts.copyCurrent().transactionScope());
  }

  private void testDerivedSubjectWhenSettingSessionImpl() throws PrivilegedActionException {
    final IServerSession session = mock(IServerSession.class);
    final Subject subject1 = new Subject();
    final Subject subject2 = new Subject();

    // Current Subject set, Subject on session not set --> Null Subject
    Subject.doAs(subject1, new PrivilegedExceptionAction<Object>() {

      @Override
      public Object run() throws Exception {
        when(session.getSubject()).thenReturn(null);
        assertNull(ServerRunContexts.copyCurrent().session(session).subject());
        return null;
      }
    });

    // Current Subject not set, Subject on session not set --> Null Subject
    Subject.doAs(null, new PrivilegedExceptionAction<Object>() {

      @Override
      public Object run() throws Exception {
        when(session.getSubject()).thenReturn(null);
        assertNull(ServerRunContexts.copyCurrent().session(session).subject());
        return null;
      }
    });

    // Current Subject not set, Subject on session set --> Subject from session
    Subject.doAs(null, new PrivilegedExceptionAction<Object>() {

      @Override
      public Object run() throws Exception {
        when(session.getSubject()).thenReturn(subject1);
        assertSame(subject1, ServerRunContexts.copyCurrent().session(session).subject());
        return null;
      }
    });

    // Current Subject set, Subject on session set --> Subject from session
    Subject.doAs(subject1, new PrivilegedExceptionAction<Object>() {

      @Override
      public Object run() throws Exception {
        when(session.getSubject()).thenReturn(subject2);
        assertSame(subject2, ServerRunContexts.copyCurrent().session(session).subject());
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
    assertNotNull(ServerRunContexts.copyCurrent());
    assertTrue(toSet(ServerRunContexts.copyCurrent().propertyMap().iterator()).isEmpty());

    // Context on ThreadLocal
    PropertyMap.CURRENT.set(propertyMap);
    assertNotSame(propertyMap, ServerRunContexts.copyCurrent().propertyMap()); // test for copy
    assertEquals(toSet(propertyMap.iterator()), toSet(ServerRunContexts.copyCurrent().propertyMap().iterator()));
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
