/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import javax.security.auth.Subject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.security.SimplePrincipalProducer;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.server.commons.authentication.ServletFilterHelper;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
@RunWithSubject(HttpRunContextProducerTest.TEST_SUBJECT_NAME)
public class HttpRunContextProducerTest {
  static final String TEST_SUBJECT_NAME = "testSubj";
  static final String TEST_CID = "abc";
  static final Locale TEST_LOCALE = Locale.CANADA_FRENCH;
  static final Subject TEST_SUBJECT = BEANS.get(ServletFilterHelper.class).createSubject(BEANS.get(SimplePrincipalProducer.class).produce(TEST_SUBJECT_NAME));

  @Test
  public void testCreateRunContextWithCid() {
    HttpRunContextProducer producer = new HttpRunContextProducer();
    HttpServletResponse resp = mock(HttpServletResponse.class);
    HttpServletRequest req = createRequestMock(TEST_CID);

    RunContext context = producer.produce(req, resp);
    assertEquals(TEST_SUBJECT.getPrincipals(), context.getSubject().getPrincipals());
    assertEquals(TEST_CID, context.getCorrelationId());
    assertSame(req, context.getThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST));
    assertSame(resp, context.getThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE));
    assertEquals(TEST_LOCALE, context.getLocale());
    assertEquals(TransactionScope.REQUIRES_NEW, context.getTransactionScope());
  }

  @Test
  public void testCreateServletRunContextWithoutSessionButWithoutCid() {
    HttpRunContextProducer producer = new HttpRunContextProducer();
    HttpServletResponse resp = mock(HttpServletResponse.class);
    HttpServletRequest req = createRequestMock(null);

    RunContext context = producer.produce(req, resp);
    assertNotNull(context.getCorrelationId());
    assertNotEquals(TEST_CID, context.getCorrelationId());
  }

  protected HttpServletRequest createRequestMock(String cid) {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getHeader(eq(CorrelationId.HTTP_HEADER_NAME))).thenReturn(cid);
    when(req.getLocale()).thenReturn(TEST_LOCALE);
    return req;
  }
}
