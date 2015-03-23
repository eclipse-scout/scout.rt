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
package org.eclipse.scout.rt.server.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
public class ServerJobInputTest {

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
  public void testFillEmpty() {
    ServerJobInput input = ServerJobInput.fillEmpty();
    assertNotNull(input.getContext());
    assertNull(input.getName());
    assertNull(input.getId());
    assertNull(input.getSubject());
    assertNull(input.getSession());
    assertTrue(input.isSessionRequired());
    assertNull(input.getUserAgent());
    assertNull(input.getLocale());
  }

  @Test
  public void testCopy() {
    ServerJobInput input = ServerJobInput.fillEmpty();
    input.getPropertyMap().put("A", "B");
    input.name("name");
    input.id("123");
    input.subject(new Subject());
    input.session(mock(IServerSession.class));
    input.userAgent(UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a"));
    input.locale(Locale.CANADA_FRENCH);

    ServerJobInput copy = input.copy();

    assertNotSame(input.getContext(), copy.getContext());
    assertEquals(toSet(input.getPropertyMap().iterator()), toSet(copy.getPropertyMap().iterator()));
    assertEquals(input.getName(), copy.getName());
    assertEquals(input.getId(), copy.getId());
    assertSame(input.getSubject(), copy.getSubject());
    assertSame(input.getUserAgent(), copy.getUserAgent());
    assertSame(input.getLocale(), copy.getLocale());
    assertSame(input.getLocale(), copy.getLocale());
  }

  @Test
  public void testFillCurrentName() {
    assertNull(ServerJobInput.fillCurrent().getName());
    assertEquals("ABC", ServerJobInput.fillCurrent().name("ABC").getName());
  }

  @Test
  public void testFillCurrentId() {
    assertNull(ServerJobInput.fillCurrent().getId());
    assertEquals("123", ServerJobInput.fillCurrent().id("123").getId());
  }

  @Test
  public void testFillCurrentSubject() {
    assertNull(ServerJobInput.fillCurrent().getSubject());

    Subject subject = new Subject();
    ServerJobInput input = Subject.doAs(subject, new PrivilegedAction<ServerJobInput>() {

      @Override
      public ServerJobInput run() {
        return ServerJobInput.fillCurrent();
      }
    });
    assertSame(subject, input.getSubject());

    subject = new Subject();
    input = Subject.doAs(subject, new PrivilegedAction<ServerJobInput>() {

      @Override
      public ServerJobInput run() {
        return ServerJobInput.fillCurrent();
      }
    });
    input.subject(null);
    assertNull(input.getSubject());
  }

  @Test
  public void testFillCurrentSessionRequired() {
    assertTrue(ServerJobInput.fillCurrent().isSessionRequired());
  }

  @Test
  public void testSessionRequiredCopy() {
    assertTrue(ServerJobInput.fillEmpty().sessionRequired(true).copy().isSessionRequired());
    assertFalse(ServerJobInput.fillEmpty().sessionRequired(false).copy().isSessionRequired());
  }

  @Test
  public void testFillCurrentSession() {
    // No session on ThreadLocal
    ISession.CURRENT.remove();
    assertNull(ServerJobInput.fillCurrent().sessionRequired(false).getSession());

    // Session on ThreadLocal
    IServerSession sessionThreadLocal = mock(IServerSession.class);
    ISession.CURRENT.set(sessionThreadLocal);
    assertSame(sessionThreadLocal, ServerJobInput.fillCurrent().getSession());

    // Session on ThreadLocal, but set explicitly
    ISession.CURRENT.set(sessionThreadLocal);
    IServerSession explicitSession = mock(IServerSession.class);
    assertSame(explicitSession, ServerJobInput.fillCurrent().session(explicitSession).getSession());

    // Session on ThreadLocal, but set explicitly to null
    ISession.CURRENT.set(sessionThreadLocal);
    assertNull(ServerJobInput.fillCurrent().session(null).sessionRequired(false).getSession());
  }

  @Test
  public void testFillCurrentLocale() {
    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set, Session available --> Locale from ThreadLocal
    ISession.CURRENT.set(session);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ServerJobInput.fillCurrent().getLocale());

    // ThreadLocal set, Session available --> Locale from ThreadLocal
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ServerJobInput.fillCurrent().getLocale());

    // ThreadLocal not set, Session not available --> no fallback to JVM default Locale.
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ServerJobInput.fillCurrent().getLocale());
  }

  @Test
  public void testFillCurrentLocaleAndSetNullLocale() {
    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set, Session available --> explicit Locale (null)
    ISession.CURRENT.set(session);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ServerJobInput.fillCurrent().locale(null).session(session).getLocale());

    // ThreadLocal set, Session not available --> explicit Locale (null)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ServerJobInput.fillCurrent().locale(null).session(session).getLocale());

    // ThreadLocal not set, Session not available --> explicit Locale (null)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ServerJobInput.fillCurrent().locale(null).session(session).getLocale());
  }

  @Test
  public void testFillCurrentLocaleAndSetNotNullLocale() {
    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set, Session available --> explicit Locale (JAPAN)
    ISession.CURRENT.set(session);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.JAPAN, ServerJobInput.fillCurrent().locale(Locale.JAPAN).session(session).getLocale());

    // ThreadLocal set, Session not available --> explicit Locale (JAPAN)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.JAPAN, ServerJobInput.fillCurrent().locale(Locale.JAPAN).session(session).getLocale());

    // ThreadLocal not set, Session not available --> explicit Locale (JAPAN)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertEquals(Locale.JAPAN, ServerJobInput.fillCurrent().locale(Locale.JAPAN).session(session).getLocale());
  }

  @Test
  public void testDerivedLocaleWhenSettingSession() {
    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set --> Locale form ThreadLocal
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ServerJobInput.fillCurrent().session(session).getLocale());

    // ThreadLocal not set --> Null Locale
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ServerJobInput.fillCurrent().session(session).getLocale());

    IServerSession currentSession = mock(IServerSession.class);

    // ThreadLocal-Session available, ThreadLocal set --> Locale from ThreadLocal
    ISession.CURRENT.set(currentSession);
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ServerJobInput.fillCurrent().session(session).getLocale());

    // ThreadLocal-Session available, ThreadLocal not set --> Null Locale
    ISession.CURRENT.set(currentSession);
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ServerJobInput.fillCurrent().session(session).getLocale());
  }

  @Test
  public void testFillCurrentUserAgent() {
    UserAgent userAgent = newUserAgent();

    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set, Session available --> UserAgent from ThreadLocal
    ISession.CURRENT.set(session);
    UserAgent.CURRENT.set(userAgent);
    assertSame(userAgent, ServerJobInput.fillCurrent().getUserAgent());

    // ThreadLocal set, Session available --> UserAgent from ThreadLocal
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent);
    assertEquals(userAgent, ServerJobInput.fillCurrent().getUserAgent());

    // ThreadLocal not set, Session not available
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ServerJobInput.fillCurrent().getUserAgent());
  }

  @Test
  public void testFillCurrentUserAgentAndSetNullUserAgent() {
    UserAgent userAgent = newUserAgent();
    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set, Session available --> explicit UserAgent (null)
    ISession.CURRENT.set(session);
    UserAgent.CURRENT.set(userAgent);
    assertNull(ServerJobInput.fillCurrent().userAgent(null).session(session).getUserAgent());

    // ThreadLocal set, Session not available --> explicit UserAgent (null)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent);
    assertNull(ServerJobInput.fillCurrent().userAgent(null).session(session).getUserAgent());

    // ThreadLocal not set, Session not available --> explicit UserAgent (null)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ServerJobInput.fillCurrent().userAgent(null).session(session).getUserAgent());
  }

  @Test
  public void testFillCurrentUserAgentAndSetNotNullUserAgent() {
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();

    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set, Session available --> explicit UserAgent (userAgent2)
    ISession.CURRENT.set(session);
    UserAgent.CURRENT.set(userAgent1);
    assertSame(userAgent2, ServerJobInput.fillCurrent().userAgent(userAgent2).session(session).getUserAgent());

    // ThreadLocal set, Session not available --> explicit UserAgent (userAgent2)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertSame(userAgent2, ServerJobInput.fillCurrent().userAgent(userAgent2).session(session).getUserAgent());

    // ThreadLocal not set, Session not available --> explicit UserAgent (userAgent2)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertSame(userAgent2, ServerJobInput.fillCurrent().userAgent(userAgent2).session(session).getUserAgent());
  }

  @Test
  public void testDerivedUserAgentWhenSettingSession() {
    UserAgent userAgent1 = newUserAgent();

    IServerSession session = mock(IServerSession.class);

    // ThreadLocal set --> UserAgent form ThreadLocal
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertSame(userAgent1, ServerJobInput.fillCurrent().session(session).getUserAgent());

    // ThreadLocal not set --> Null UserAgent
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ServerJobInput.fillCurrent().session(session).getUserAgent());

    IServerSession currentSession = mock(IServerSession.class);

    // ThreadLocal-Session available, ThreadLocal set --> UserAgent from ThreadLocal
    ISession.CURRENT.set(currentSession);
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertSame(userAgent1, ServerJobInput.fillCurrent().session(session).getUserAgent());

    // ThreadLocal-Session available, ThreadLocal not set --> Null UserAgent
    ISession.CURRENT.set(currentSession);
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ServerJobInput.fillCurrent().session(session).getUserAgent());
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

  private void testDerivedSubjectWhenSettingSessionImpl() throws PrivilegedActionException {
    final IServerSession session = mock(IServerSession.class);
    final Subject subject1 = new Subject();

    // Current Subject set, Subject on session not set --> Current Subject
    Subject.doAs(subject1, new PrivilegedExceptionAction<Object>() {

      @Override
      public Object run() throws Exception {
        when(session.getSubject()).thenReturn(null);
        assertSame(subject1, ServerJobInput.fillCurrent().session(session).getSubject());
        return null;
      }
    });

    // Current Subject not set, Subject on session set --> NULL Subject
    Subject.doAs(null, new PrivilegedExceptionAction<Object>() {

      @Override
      public Object run() throws Exception {
        when(session.getSubject()).thenReturn(subject1);
        assertNull(ServerJobInput.fillCurrent().session(session).getSubject());
        return null;
      }
    });
  }

  @Test
  public void testFillCurrentPropertyMap() {
    PropertyMap threadLocalContext = new PropertyMap();
    threadLocalContext.put("prop", "value");

    // No context on ThreadLocal
    PropertyMap.CURRENT.remove();
    assertNotNull(ServerJobInput.fillCurrent().getContext());

    // Context on ThreadLocal
    PropertyMap.CURRENT.set(threadLocalContext);
    assertNotSame(threadLocalContext, ServerJobInput.fillCurrent().getContext());
    assertEquals(toSet(threadLocalContext.iterator()), toSet(ServerJobInput.fillCurrent().getPropertyMap().iterator()));
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
