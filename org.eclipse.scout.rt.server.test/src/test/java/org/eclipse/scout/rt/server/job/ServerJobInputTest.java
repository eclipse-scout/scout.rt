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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
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
public class ServerJobInputTest {

  @Before
  public void before() {
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    ScoutTexts.CURRENT.remove();
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.remove();
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.remove();
    UserAgent.CURRENT.remove();
  }

  @Test
  public void testEmpty() {
    ServerJobInput input = ServerJobInput.empty();
    assertNotNull(input.getContext());
    assertNull(input.getName());
    assertEquals(0, input.getId());
    assertNull(input.getSubject());
    assertNull(input.getServletRequest());
    assertNull(input.getServletResponse());
    assertNull(input.getSession());
    assertNull(input.getUserAgent());
    assertNull(input.getLocale());
  }

  @Test
  public void testCopy() {
    ServerJobInput input = ServerJobInput.empty();
    input.getContext().set("A", "B");
    input.name("name");
    input.id(123);
    input.subject(new Subject());
    input.servletRequest(mock(HttpServletRequest.class));
    input.servletResponse(mock(HttpServletResponse.class));
    input.session(mock(IServerSession.class));
    input.userAgent(UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a"));
    input.locale(Locale.CANADA_FRENCH);

    ServerJobInput copy = input.copy();

    assertNotSame(input.getContext(), copy.getContext());
    assertEquals(toSet(input.getContext().iterator()), toSet(copy.getContext().iterator()));
    assertEquals(input.getName(), copy.getName());
    assertEquals(input.getId(), copy.getId());
    assertSame(input.getSubject(), copy.getSubject());
    assertSame(input.getServletRequest(), copy.getServletRequest());
    assertSame(input.getServletResponse(), copy.getServletResponse());
    assertSame(input.getUserAgent(), copy.getUserAgent());
    assertSame(input.getLocale(), copy.getLocale());
  }

  @Test
  public void testDefaultName() {
    assertNull(ServerJobInput.defaults().getName());
    assertEquals("ABC", ServerJobInput.defaults().name("ABC").getName());
  }

  @Test
  public void testDefaultId() {
    assertEquals(0, ServerJobInput.defaults().getId());
    assertEquals(123, ServerJobInput.defaults().id(123).getId());
  }

  @Test
  public void testDefaultSubject() {
    assertNull(ServerJobInput.defaults().getSubject());

    Subject subject = new Subject();
    ServerJobInput input = Subject.doAs(subject, new PrivilegedAction<ServerJobInput>() {

      @Override
      public ServerJobInput run() {
        return ServerJobInput.defaults();
      }
    });
    assertSame(subject, input.getSubject());

    subject = new Subject();
    input = Subject.doAs(subject, new PrivilegedAction<ServerJobInput>() {

      @Override
      public ServerJobInput run() {
        return ServerJobInput.defaults();
      }
    });
    input.subject(null);
    assertNull(input.getSubject());
  }

  @Test
  public void testDefaultServletRequest() {
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.remove();
    assertNull(ServerJobInput.defaults().getServletRequest());

    HttpServletRequest request = mock(HttpServletRequest.class);
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.set(request);
    assertSame(request, ServerJobInput.defaults().getServletRequest());

    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.set(request);
    assertNull(ServerJobInput.defaults().servletRequest(null).getServletRequest());
  }

  @Test
  public void testDefaultServletResponse() {
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.remove();
    assertNull(ServerJobInput.defaults().getServletResponse());

    HttpServletResponse response = mock(HttpServletResponse.class);
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.set(response);
    assertSame(response, ServerJobInput.defaults().getServletResponse());

    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.set(response);
    assertNull(ServerJobInput.defaults().servletResponse(null).getServletResponse());
  }

  @Test
  public void testDefaultSession() {
    ISession.CURRENT.remove();
    assertNull(ServerJobInput.defaults().getSession());

    IServerSession session = mock(IServerSession.class);
    ISession.CURRENT.set(session);
    assertSame(session, ServerJobInput.defaults().getSession());

    ISession.CURRENT.set(session);
    assertNull(ServerJobInput.defaults().session(null).getSession());
  }

  @Test
  public void testDefaultJobContext() {
    JobContext ctx = new JobContext();
    ctx.set("prop", "value");

    JobContext.CURRENT.remove();
    assertNotNull(ServerJobInput.defaults().getContext());

    JobContext.CURRENT.set(ctx);
    assertNotNull(ctx);
    assertNotSame(ctx, ServerJobInput.defaults().getContext());
    assertEquals(toSet(ctx.iterator()), toSet(ServerJobInput.defaults().getContext().iterator()));

    JobContext.CURRENT.set(ctx);
    assertNull(ServerJobInput.defaults().context(null).getContext());
  }

  @Test
  public void testDefaultLocale() {
    NlsLocale.CURRENT.remove();
    assertNull(ServerJobInput.defaults().getLocale());

    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, ServerJobInput.defaults().getLocale());
  }

  @Test
  public void testUserAgent() {
    UserAgent.CURRENT.remove();
    assertNull(ServerJobInput.defaults().getUserAgent());

    UserAgent userAgent = UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a");
    UserAgent.CURRENT.set(userAgent);
    assertSame(userAgent, ServerJobInput.defaults().getUserAgent());
  }

  private static Set<Object> toSet(Iterator<?> iterator) {
    Set<Object> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }
}
