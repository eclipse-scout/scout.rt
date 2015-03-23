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

import org.eclipse.scout.commons.Assertions.AssertionException;
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
public class ModelJobInputTest {

  @Before
  public void before() {
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    ScoutTexts.CURRENT.remove();
    UserAgent.CURRENT.remove();
  }

  @Test(expected = AssertionException.class)
  public void testMutex1() {
    IClientSession session = mock(IClientSession.class);
    ISession.CURRENT.set(session);
    assertNull(ModelJobInput.fillEmpty().getMutex());
  }

  @Test
  public void testMutex2() {
    IClientSession session = mock(IClientSession.class);
    ISession.CURRENT.set(session);
    assertSame(session, ModelJobInput.fillCurrent().getMutex());
  }

  @Test
  public void testMutex3() {
    IClientSession session1 = mock(IClientSession.class);
    IClientSession session2 = mock(IClientSession.class);
    ISession.CURRENT.set(session1);
    assertSame(session2, ModelJobInput.fillCurrent().session(session2).getMutex());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testMutex4() {
    ModelJobInput.fillCurrent().mutex(new Object());
  }

  @Test(expected = AssertionException.class)
  public void testSessionRequired1() {
    ModelJobInput.fillEmpty().sessionRequired(false);
  }

  @Test(expected = AssertionException.class)
  public void testSessionRequired2() {
    ModelJobInput.fillEmpty().sessionRequired(false);
  }

  @Test(expected = AssertionException.class)
  public void testSessionRequired3() {
    ModelJobInput.fillEmpty().session(null);
  }

  @Test(expected = AssertionException.class)
  public void testSessionRequired4() {
    ModelJobInput.fillCurrent().session(null);
  }

  @Test
  public void testSessionRequired5() {
    assertNull(ModelJobInput.fillEmpty().copy().getSession()); // no assertion exception expected
    assertNull(ModelJobInput.fillCurrent().copy().getSession()); // no assertion exception expected
  }

  // === Test methods of ModelJobInputTest ===

  @Test
  public void testFillEmpty() {
    ModelJobInput input = ModelJobInput.fillEmpty();
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
    ModelJobInput input = ModelJobInput.fillEmpty();
    input.getPropertyMap().put("A", "B");
    input.name("name");
    input.id("123");
    input.subject(new Subject());
    input.session(mock(IClientSession.class));
    input.userAgent(UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a"));
    input.locale(Locale.CANADA_FRENCH);

    ModelJobInput copy = input.copy();

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
    assertNull(ModelJobInput.fillCurrent().getName());
    assertEquals("ABC", ModelJobInput.fillCurrent().name("ABC").getName());
  }

  @Test
  public void testFillCurrentId() {
    assertNull(ModelJobInput.fillCurrent().getId());
    assertEquals("123", ModelJobInput.fillCurrent().id("123").getId());
  }

  @Test
  public void testFillCurrentSubject() {
    assertNull(ModelJobInput.fillCurrent().getSubject());

    Subject subject = new Subject();
    ModelJobInput input = Subject.doAs(subject, new PrivilegedAction<ModelJobInput>() {

      @Override
      public ModelJobInput run() {
        return ModelJobInput.fillCurrent();
      }
    });
    assertSame(subject, input.getSubject());

    subject = new Subject();
    input = Subject.doAs(subject, new PrivilegedAction<ModelJobInput>() {

      @Override
      public ModelJobInput run() {
        return ModelJobInput.fillCurrent();
      }
    });
    input.subject(null);
    assertNull(input.getSubject());
  }

  @Test
  public void testFillCurrentSessionRequired() {
    assertTrue(ModelJobInput.fillCurrent().isSessionRequired());
  }

  @Test
  public void testSessionRequiredCopy() {
    assertTrue(ModelJobInput.fillEmpty().sessionRequired(true).copy().isSessionRequired());
  }

  @Test
  public void testFillCurrentSession() {
    // No session on ThreadLocal
    ISession.CURRENT.remove();

    // Session on ThreadLocal
    IClientSession sessionThreadLocal = mock(IClientSession.class);
    ISession.CURRENT.set(sessionThreadLocal);
    assertSame(sessionThreadLocal, ModelJobInput.fillCurrent().getSession());

    // Session on ThreadLocal, but set explicitly
    ISession.CURRENT.set(sessionThreadLocal);
    IClientSession explicitSession = mock(IClientSession.class);
    assertSame(explicitSession, ModelJobInput.fillCurrent().session(explicitSession).getSession());
  }

  @Test
  public void testFillCurrentLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale --> Locale from session
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.ITALY, ModelJobInput.fillCurrent().getLocale());

    // ThreadLocal set, Session set with null Locale --> Null Locale from session
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ModelJobInput.fillCurrent().getLocale());

    // ThreadLocal set, Session not set --> Locale from ThreadLocal
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ModelJobInput.fillCurrent().getLocale());

    // ThreadLocal not set, Session not set --> no fallback to JVM default Locale.
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ModelJobInput.fillCurrent().getLocale());
  }

  @Test
  public void testFillCurrentLocaleAndSetNullLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale --> explicit Locale (null)
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ModelJobInput.fillCurrent().locale(null).session(session).getLocale());

    // ThreadLocal set, Session set with null Locale --> explicit Locale (null)
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ModelJobInput.fillCurrent().locale(null).session(session).getLocale());

    // ThreadLocal set, Session not set --> explicit Locale (null)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertNull(ModelJobInput.fillCurrent().locale(null).session(session).getLocale());

    // ThreadLocal not set, Session not set --> explicit Locale (null)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertNull(ModelJobInput.fillCurrent().locale(null).session(session).getLocale());
  }

  @Test
  public void testFillCurrentLocaleAndSetNotNullLocale() {
    IClientSession session = mock(IClientSession.class);

    // ThreadLocal set, Session set with Locale --> explicit Locale (JAPAN)
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.ITALY);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.JAPAN, ModelJobInput.fillCurrent().locale(Locale.JAPAN).session(session).getLocale());

    // ThreadLocal set, Session set with null Locale --> explicit Locale (JAPAN)
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.JAPAN, ModelJobInput.fillCurrent().locale(Locale.JAPAN).session(session).getLocale());

    // ThreadLocal set, Session not set --> explicit Locale (JAPAN)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.JAPAN, ModelJobInput.fillCurrent().locale(Locale.JAPAN).session(session).getLocale());

    // ThreadLocal not set, Session not set --> explicit Locale (JAPAN)
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    assertEquals(Locale.JAPAN, ModelJobInput.fillCurrent().locale(Locale.JAPAN).session(session).getLocale());
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
    assertNull(ModelJobInput.fillCurrent().session(session).getLocale());

    // ThreadLocal not set, Locale on session not set --> Null Locale
    NlsLocale.CURRENT.remove();
    when(session.getLocale()).thenReturn(null);
    assertNull(ModelJobInput.fillCurrent().session(session).getLocale());

    // ThreadLocal not set, Locale on session set --> Locale from session
    NlsLocale.CURRENT.remove();
    when(session.getLocale()).thenReturn(Locale.CHINESE);
    assertEquals(Locale.CHINESE, ModelJobInput.fillCurrent().session(session).getLocale());

    // ThreadLocal set, Locale on session set --> Locale from session
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    when(session.getLocale()).thenReturn(Locale.CHINESE);
    assertEquals(Locale.CHINESE, ModelJobInput.fillCurrent().session(session).getLocale());
  }

  @Test
  public void testFillCurrentUserAgent() {
    IClientSession session = mock(IClientSession.class);
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();

    // ThreadLocal set, Session set with UserAgent --> UserAgent from session
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.set(userAgent2);
    assertSame(userAgent1, ModelJobInput.fillCurrent().getUserAgent());

    // ThreadLocal set, Session set with null UserAgent --> Null UserAgent from session
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(null);
    UserAgent.CURRENT.set(userAgent1);
    assertNull(ModelJobInput.fillCurrent().getUserAgent());

    // ThreadLocal set, Session not set --> UserAgent from ThreadLocal
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertSame(userAgent1, ModelJobInput.fillCurrent().getUserAgent());

    // ThreadLocal not set, Session not set --> Null User Agent
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ModelJobInput.fillCurrent().getUserAgent());
  }

  @Test
  public void testFillCurrentUserAgentAndSetNullUserAgent() {
    IClientSession session = mock(IClientSession.class);
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();

    // ThreadLocal set, Session set with UserAgent --> explicit UserAgent (null)
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.set(userAgent2);
    assertNull(ModelJobInput.fillCurrent().userAgent(null).session(session).getUserAgent());

    // ThreadLocal set, Session set with null UserAgent --> explicit UserAgent (null)
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(null);
    UserAgent.CURRENT.set(userAgent2);
    assertNull(ModelJobInput.fillCurrent().userAgent(null).session(session).getUserAgent());

    // ThreadLocal set, Session not set --> explicit UserAgent (null)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent2);
    assertNull(ModelJobInput.fillCurrent().userAgent(null).session(session).getUserAgent());

    // ThreadLocal not set, Session not set --> explicit UserAgent (null)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertNull(ModelJobInput.fillCurrent().userAgent(null).session(session).getUserAgent());
  }

  @Test
  public void testFillCurrentUserAgentAndSetNotNullUserAgent() {
    IClientSession session = mock(IClientSession.class);
    UserAgent userAgent1 = newUserAgent();
    UserAgent userAgent2 = newUserAgent();
    UserAgent userAgent3 = newUserAgent();

    // ThreadLocal set, Session set with UserAgent --> explicit UserAgent (JAPAN)
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(userAgent1);
    UserAgent.CURRENT.set(userAgent2);
    assertEquals(userAgent3, ModelJobInput.fillCurrent().userAgent(userAgent3).session(session).getUserAgent());

    // ThreadLocal set, Session set with null UserAgent --> explicit UserAgent (JAPAN)
    ISession.CURRENT.set(session);
    when(session.getUserAgent()).thenReturn(null);
    UserAgent.CURRENT.set(userAgent1);
    assertEquals(userAgent3, ModelJobInput.fillCurrent().userAgent(userAgent3).session(session).getUserAgent());

    // ThreadLocal set, Session not set --> explicit UserAgent (JAPAN)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.set(userAgent1);
    assertEquals(userAgent3, ModelJobInput.fillCurrent().userAgent(userAgent3).session(session).getUserAgent());

    // ThreadLocal not set, Session not set --> explicit UserAgent (JAPAN)
    ISession.CURRENT.remove();
    UserAgent.CURRENT.remove();
    assertEquals(userAgent3, ModelJobInput.fillCurrent().userAgent(userAgent3).session(session).getUserAgent());
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
    assertNull(ModelJobInput.fillCurrent().session(session).getUserAgent());

    // ThreadLocal not set, UserAgent on session not set --> Null UserAgent
    UserAgent.CURRENT.remove();
    when(session.getUserAgent()).thenReturn(null);
    assertNull(ModelJobInput.fillCurrent().session(session).getUserAgent());

    // ThreadLocal not set, UserAgent on session set --> UserAgent from session
    UserAgent.CURRENT.remove();
    when(session.getUserAgent()).thenReturn(userAgent1);
    assertEquals(userAgent1, ModelJobInput.fillCurrent().session(session).getUserAgent());

    // ThreadLocal set, UserAgent on session set --> UserAgent from session
    UserAgent.CURRENT.set(userAgent1);
    when(session.getUserAgent()).thenReturn(userAgent2);
    assertEquals(userAgent2, ModelJobInput.fillCurrent().session(session).getUserAgent());
  }

  @Test
  public void testFillCurrentPropertyMap() {
    PropertyMap threadLocalContext = new PropertyMap();
    threadLocalContext.put("prop", "value");

    // No context on ThreadLocal
    PropertyMap.CURRENT.remove();
    assertNotNull(ModelJobInput.fillCurrent().getContext());

    // Context on ThreadLocal
    PropertyMap.CURRENT.set(threadLocalContext);
    assertNotSame(threadLocalContext, ModelJobInput.fillCurrent().getContext());
    assertEquals(toSet(threadLocalContext.iterator()), toSet(ModelJobInput.fillCurrent().getPropertyMap().iterator()));
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
