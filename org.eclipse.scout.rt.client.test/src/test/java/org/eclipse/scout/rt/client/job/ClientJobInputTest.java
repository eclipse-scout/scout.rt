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

import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.ScoutPlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutPlatformTestRunner.class)
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
    assertEquals(0, input.getId());
    assertNull(input.getSubject());
    assertNull(input.getSession());
    assertTrue(input.isSessionRequired());
    assertNull(input.getUserAgent());
    assertNull(input.getLocale());
  }

  @Test
  public void testCopy() {
    ClientJobInput input = ClientJobInput.empty();
    input.getContext().set("A", "B");
    input.name("name");
    input.id(123);
    input.subject(new Subject());
    input.session(mock(IClientSession.class));
    input.userAgent(UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a"));
    input.locale(Locale.CANADA_FRENCH);

    ClientJobInput copy = input.copy();

    assertNotSame(input.getContext(), copy.getContext());
    assertEquals(toSet(input.getContext().iterator()), toSet(copy.getContext().iterator()));
    assertEquals(input.getName(), copy.getName());
    assertEquals(input.getId(), copy.getId());
    assertSame(input.getSubject(), copy.getSubject());
    assertSame(input.getUserAgent(), copy.getUserAgent());
    assertSame(input.getLocale(), copy.getLocale());
  }

  @Test
  public void testDefaultName() {
    assertNull(ClientJobInput.defaults().getName());
    assertEquals("ABC", ClientJobInput.defaults().name("ABC").getName());
  }

  @Test
  public void testDefaultId() {
    assertEquals(0, ClientJobInput.defaults().getId());
    assertEquals(123, ClientJobInput.defaults().id(123).getId());
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
    input.subject(null);
    assertNull(input.getSubject());
  }

  @Test
  public void testDefaultSession() {
    ISession.CURRENT.remove();
    assertNull(ClientJobInput.defaults().getSession());

    IClientSession session = mock(IClientSession.class);
    ISession.CURRENT.set(session);
    assertSame(session, ClientJobInput.defaults().getSession());

    ISession.CURRENT.set(session);
    assertNull(ClientJobInput.defaults().session(null).getSession());
  }

  @Test
  public void testDefaultSessionRequired() {
    assertTrue(ClientJobInput.defaults().isSessionRequired());
  }

  @Test
  public void testDefaultJobContext() {
    JobContext ctx = new JobContext();
    ctx.set("prop", "value");

    JobContext.CURRENT.remove();
    assertNotNull(ClientJobInput.defaults().getContext());

    JobContext.CURRENT.set(ctx);
    assertNotNull(ctx);
    assertNotSame(ctx, ClientJobInput.defaults().getContext());
    assertEquals(toSet(ctx.iterator()), toSet(ClientJobInput.defaults().getContext().iterator()));

    JobContext.CURRENT.set(ctx);
    assertNull(ClientJobInput.defaults().context(null).getContext());
  }

  @Test
  public void testDefaultLocale() {
    IClientSession session = mock(IClientSession.class);

    // Test no session and no current thread Locale
    NlsLocale.CURRENT.remove();
    ISession.CURRENT.remove();
    assertEquals(Locale.getDefault(), ClientJobInput.defaults().getLocale());

    // Test session without Locale and no current thread Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.remove();
    assertEquals(Locale.getDefault(), ClientJobInput.defaults().getLocale());

    // Test session with Locale and no current thread Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.CANADA_FRENCH);
    NlsLocale.CURRENT.remove();
    ISession.CURRENT.set(session);
    assertEquals(Locale.CANADA_FRENCH, ClientJobInput.defaults().getLocale());

    // Test session with Locale and current thread Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(Locale.CANADA_FRENCH);
    NlsLocale.CURRENT.set(Locale.CHINA);
    assertEquals(Locale.CANADA_FRENCH, ClientJobInput.defaults().getLocale());

    // Test no session and current thread Locale
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.set(Locale.CHINA);
    assertEquals(Locale.CHINA, ClientJobInput.defaults().getLocale());

    // Test session with no Locale and current thread Locale
    ISession.CURRENT.set(session);
    when(session.getLocale()).thenReturn(null);
    NlsLocale.CURRENT.set(Locale.CHINA);
    assertEquals(Locale.CHINA, ClientJobInput.defaults().getLocale());
  }

  @Test
  public void testUserAgent() {
    UserAgent.CURRENT.remove();
    assertNull(ClientJobInput.defaults().getUserAgent());

    UserAgent userAgent = UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a");
    UserAgent.CURRENT.set(userAgent);
    assertSame(userAgent, ClientJobInput.defaults().getUserAgent());
  }

  private static Set<Object> toSet(Iterator<?> iterator) {
    Set<Object> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }
}
