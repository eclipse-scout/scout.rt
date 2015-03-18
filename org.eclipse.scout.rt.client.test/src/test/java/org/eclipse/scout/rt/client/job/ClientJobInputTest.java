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
package org.eclipse.scout.rt.client.job;

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
public class ClientJobInputTest {

  @Before
  public void before() {
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    ScoutTexts.CURRENT.remove();
    UserAgent.CURRENT.remove();
  }

  @Test
  public void testEmpty() {
    ClientJobInput input = ClientJobInput.empty();
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
    ClientJobInput input = ClientJobInput.empty();
    input.getPropertyMap().put("A", "B");
    input.setName("name");
    input.setId("123");
    input.setSubject(new Subject());
    input.setSession(mock(IClientSession.class));
    input.setUserAgent(UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a"));
    input.setLocale(Locale.CANADA_FRENCH);

    ClientJobInput copy = input.copy();

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
  public void testDefaultName() {
    assertNull(ClientJobInput.defaults().getName());
    assertEquals("ABC", ClientJobInput.defaults().setName("ABC").getName());
  }

  @Test
  public void testDefaultId() {
    assertNull(ClientJobInput.defaults().getId());
    assertEquals("123", ClientJobInput.defaults().setId("123").getId());
  }

  @Test
  public void testDefaultSubject() {
    assertNull(ClientJobInput.defaults().getSubject());

    Subject subject = new Subject();
    ClientJobInput input = Subject.doAs(subject, new PrivilegedAction<ClientJobInput>() {

      @Override
      public ClientJobInput run() {
        return ClientJobInput.defaults();
      }
    });
    assertSame(subject, input.getSubject());

    subject = new Subject();
    input = Subject.doAs(subject, new PrivilegedAction<ClientJobInput>() {

      @Override
      public ClientJobInput run() {
        return ClientJobInput.defaults();
      }
    });
    input.setSubject(null);
    assertNull(input.getSubject());
  }

  @Test
  public void testDefaultSessionRequired() {
    assertTrue(ClientJobInput.defaults().isSessionRequired());
  }

  @Test
  public void testSessionRequiredCopy() {
    assertTrue(ClientJobInput.empty().setSessionRequired(true).copy().isSessionRequired());
    assertFalse(ClientJobInput.empty().setSessionRequired(false).copy().isSessionRequired());
  }

  @Test
  public void testDefaultSession() {
    // No session on ThreadLocal
    ISession.CURRENT.remove();
    assertNull(ClientJobInput.defaults().setSessionRequired(false).getSession());

    // Session on ThreadLocal
    IClientSession sessionThreadLocal = mock(IClientSession.class);
    ISession.CURRENT.set(sessionThreadLocal);
    assertSame(sessionThreadLocal, ClientJobInput.defaults().getSession());

    // Session on ThreadLocal, but set explicitly
    ISession.CURRENT.set(sessionThreadLocal);
    IClientSession explicitSession = mock(IClientSession.class);
    assertSame(explicitSession, ClientJobInput.defaults().setSession(explicitSession).getSession());

    // Session on ThreadLocal, but set explicitly to null
    ISession.CURRENT.set(sessionThreadLocal);
    assertNull(ClientJobInput.defaults().setSession(null).setSessionRequired(false).getSession());
  }

  @Test
  public void testDefaultLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale --> Locale from session
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.ITALY, ClientJobInput.defaults().getLocale());

    // ThreadLocal set, Session set with null Locale --> Null Locale from session
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientJobInput.defaults().getLocale());

    // ThreadLocal set, Session not set --> Locale from ThreadLocal
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ClientJobInput.defaults().getLocale());

    // ThreadLocal not set, Session not set --> no fallback to JVM default Locale.
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ClientJobInput.defaults().getLocale());
  }

  @Test
  public void testDefaultLocaleAndSetNullLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale --> explicit Locale (null)
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientJobInput.defaults().setLocale(null).setSession(session).getLocale());

    // ThreadLocal set, Session set with null Locale --> explicit Locale (null)
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientJobInput.defaults().setLocale(null).setSession(session).getLocale());

    // ThreadLocal set, Session not set --> explicit Locale (null)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ClientJobInput.defaults().setLocale(null).setSession(session).getLocale());

    // ThreadLocal not set, Session not set --> explicit Locale (null)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ClientJobInput.defaults().setLocale(null).setSession(session).getLocale());
  }

  @Test
  public void testDefaultLocaleAndSetNotNullLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale --> explicit Locale (JAPAN)
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.JAPAN, ClientJobInput.defaults().setLocale(Locale.JAPAN).setSession(session).getLocale());

    // ThreadLocal set, Session set with null Locale --> explicit Locale (JAPAN)
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.JAPAN, ClientJobInput.defaults().setLocale(Locale.JAPAN).setSession(session).getLocale());

    // ThreadLocal set, Session not set --> explicit Locale (JAPAN)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.JAPAN, ClientJobInput.defaults().setLocale(Locale.JAPAN).setSession(session).getLocale());

    // ThreadLocal not set, Session not set --> explicit Locale (JAPAN)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertEquals(Locale.JAPAN, ClientJobInput.defaults().setLocale(Locale.JAPAN).setSession(session).getLocale());
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
    assertNull(ClientJobInput.defaults().setSession(session).getLocale());

    // ThreadLocal not set, Locale on session not set --> Null Locale
    NlsLocale.CURRENT.remove();
    when(session.getLocale()).thenReturn(null);
    assertNull(ClientJobInput.defaults().setSession(session).getLocale());

    // ThreadLocal not set, Locale on session set --> Locale from session
    NlsLocale.CURRENT.remove();
    when(session.getLocale()).thenReturn(Locale.CHINESE);
    assertEquals(Locale.CHINESE, ClientJobInput.defaults().setSession(session).getLocale());

    // ThreadLocal set, Locale on session set --> Locale from session
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    when(session.getLocale()).thenReturn(Locale.CHINESE);
    assertEquals(Locale.CHINESE, ClientJobInput.defaults().setSession(session).getLocale());
  }

  @Test
  public void testDefaultUserAgent() {
    IClientSession session = mock(IClientSession.class);
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();

    // ThreadLocal set, Session set with UserAgent --> UserAgent from session
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.set(userAgent2);
    assertSame(userAgent1, ClientJobInput.defaults().getUserAgent());

    // ThreadLocal set, Session set with null UserAgent --> Null UserAgent from session
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(null);
    UserAgent.CURRENT.set(userAgent1);
    assertNull(ClientJobInput.defaults().getUserAgent());

    // ThreadLocal set, Session not set --> UserAgent from ThreadLocal
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertSame(userAgent1, ClientJobInput.defaults().getUserAgent());

    // ThreadLocal not set, Session not set --> Null User Agent
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ClientJobInput.defaults().getUserAgent());
  }

  @Test
  public void testDefaultUserAgentAndSetNullUserAgent() {
    IClientSession session = mock(IClientSession.class);
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();

    // ThreadLocal set, Session set with UserAgent --> explicit UserAgent (null)
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.set(userAgent2);
    assertNull(ClientJobInput.defaults().setUserAgent(null).setSession(session).getUserAgent());

    // ThreadLocal set, Session set with null UserAgent --> explicit UserAgent (null)
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(null);
    UserAgent.CURRENT.set(userAgent2);
    assertNull(ClientJobInput.defaults().setUserAgent(null).setSession(session).getUserAgent());

    // ThreadLocal set, Session not set --> explicit UserAgent (null)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent2);
    assertNull(ClientJobInput.defaults().setUserAgent(null).setSession(session).getUserAgent());

    // ThreadLocal not set, Session not set --> explicit UserAgent (null)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ClientJobInput.defaults().setUserAgent(null).setSession(session).getUserAgent());
  }

  @Test
  public void testDefaultUserAgentAndSetNotNullUserAgent() {
    IClientSession session = mock(IClientSession.class);
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();
    UserAgent userAgent3 = newUserAgent();

    // ThreadLocal set, Session set with UserAgent --> explicit UserAgent (JAPAN)
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.set(userAgent2);
    assertEquals(userAgent3, ClientJobInput.defaults().setUserAgent(userAgent3).setSession(session).getUserAgent());

    // ThreadLocal set, Session set with null UserAgent --> explicit UserAgent (JAPAN)
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(null);
    UserAgent.CURRENT.set(userAgent1);
    assertEquals(userAgent3, ClientJobInput.defaults().setUserAgent(userAgent3).setSession(session).getUserAgent());

    // ThreadLocal set, Session not set --> explicit UserAgent (JAPAN)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertEquals(userAgent3, ClientJobInput.defaults().setUserAgent(userAgent3).setSession(session).getUserAgent());

    // ThreadLocal not set, Session not set --> explicit UserAgent (JAPAN)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertEquals(userAgent3, ClientJobInput.defaults().setUserAgent(userAgent3).setSession(session).getUserAgent());
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
    assertNull(ClientJobInput.defaults().setSession(session).getUserAgent());

    // ThreadLocal not set, UserAgent on session not set --> Null UserAgent
    UserAgent.CURRENT.remove();
    when(session.getUserAgent()).thenReturn(null);
    assertNull(ClientJobInput.defaults().setSession(session).getUserAgent());

    // ThreadLocal not set, UserAgent on session set --> UserAgent from session
    UserAgent.CURRENT.remove();
    when(session.getUserAgent()).thenReturn(userAgent1);
    assertEquals(userAgent1, ClientJobInput.defaults().setSession(session).getUserAgent());

    // ThreadLocal set, UserAgent on session set --> UserAgent from session
    UserAgent.CURRENT.set(userAgent1);
    when(session.getUserAgent()).thenReturn(userAgent2);
    assertEquals(userAgent2, ClientJobInput.defaults().setSession(session).getUserAgent());
  }

  @Test
  public void testDefaultPropertyMap() {
    PropertyMap threadLocalContext = new PropertyMap();
    threadLocalContext.put("prop", "value");

    // No context on ThreadLocal
    PropertyMap.CURRENT.remove();
    assertNotNull(ClientJobInput.defaults().getContext());

    // Context on ThreadLocal
    PropertyMap.CURRENT.set(threadLocalContext);
    assertNotSame(threadLocalContext, ClientJobInput.defaults().getContext());
    assertEquals(toSet(threadLocalContext.iterator()), toSet(ClientJobInput.defaults().getPropertyMap().iterator()));
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
