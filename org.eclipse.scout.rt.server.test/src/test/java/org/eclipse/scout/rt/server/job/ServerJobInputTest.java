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

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
import org.eclipse.scout.rt.shared.ISession;
import org.junit.Test;

public class ServerJobInputTest {

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
  }

  /**
   * Tests defaults for ServerJobInput in empty calling context.
   */
  @Test
  public void testDefault1() {
    ServerJobInput input = ServerJobInput.defaults();
    assertNotNull(input.getContext());
    assertNull(input.getName());
    assertEquals(0, input.getId());
    assertNull(input.getSubject());
    assertNull(input.getServletRequest());
    assertNull(input.getServletResponse());
    assertNull(input.getSession());
  }

  /**
   * Tests defaults for ServerJobInput in valid calling context.
   */
  @Test
  public void testDefault2() {
    ISession.CURRENT.set(mock(IServerSession.class));
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.set(mock(HttpServletRequest.class));
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.set(mock(HttpServletResponse.class));
    Subject subject = new Subject();

    JobContext ctx = new JobContext();
    ctx.set("prop", "value");
    JobContext.CURRENT.set(new JobContext());

    try {
      ServerJobInput input = Subject.doAs(subject, new PrivilegedAction<ServerJobInput>() {

        @Override
        public ServerJobInput run() {
          return ServerJobInput.defaults();
        }
      });

      assertNotSame(JobContext.CURRENT.get(), input.getContext());
      assertEquals(JobContext.CURRENT.get().iterator(), input.getContext().iterator());
      assertNull(input.getName());
      assertEquals(0, input.getId());
      assertSame(subject, input.getSubject());
      assertSame(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get(), input.getServletRequest());
      assertSame(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get(), input.getServletResponse());
      assertSame(ISession.CURRENT.get(), input.getSession());
    }
    finally {
      ISession.CURRENT.remove();
      IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.remove();
      IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.remove();
    }
  }
}
