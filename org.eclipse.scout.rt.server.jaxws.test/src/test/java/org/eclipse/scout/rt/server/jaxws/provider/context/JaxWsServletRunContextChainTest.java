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
package org.eclipse.scout.rt.server.jaxws.provider.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.Iterator;

import org.eclipse.scout.commons.ThreadLocalProcessor;
import org.eclipse.scout.commons.chain.IChainable;
import org.eclipse.scout.commons.chain.InvocationChain;
import org.eclipse.scout.commons.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.commons.security.SubjectProcessor;
import org.eclipse.scout.rt.platform.context.PropertyMap;
import org.eclipse.scout.rt.platform.context.RunContextIdentifiers;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JaxWsServletRunContextChainTest {

  /**
   * Tests the correct order of interceptors in {@link JaxWsServletRunContext}.
   */
  @Test
  public void testCallableChain() throws Exception {
    InvocationChain<Object> chain = new InvocationChain<Object>();
    new JaxWsServletRunContext().interceptInvocationChain(chain);

    Iterator<IChainable> chainIterator = chain.values().iterator();

    // 1. ThreadLocalProcessor for RunMonitor.CURRENT
    IChainable c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(RunMonitor.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 2. SubjectProcessor
    c = (IChainable) chainIterator.next();
    assertEquals(SubjectProcessor.class, c.getClass());

    // 3. DiagnosticContextValueProcessor
    c = chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("subject.principal.name", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 4. ThreadLocalProcessor for NlsLocale.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(NlsLocale.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 5. ThreadLocalProcessor for PropertyMap.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(PropertyMap.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 6. ThreadLocalProcessor for RunContextIdentifiers.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(RunContextIdentifiers.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 7. ThreadLocalProcessor for IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, ((ThreadLocalProcessor) c).getThreadLocal());

    // 8. ThreadLocalProcessor for IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, ((ThreadLocalProcessor) c).getThreadLocal());

    // 9. DiagnosticContextValueProcessor
    c = chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("http.session.id", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 10. DiagnosticContextValueProcessor
    c = chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("http.request.uri", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 11. DiagnosticContextValueProcessor
    c = chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("http.request.method", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 12. ThreadLocalProcessor for JaxWsRunContext.CURRENT_WEBSERVICE_CONTEXT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(JaxWsServletRunContext.CURRENT_WEBSERVICE_CONTEXT, ((ThreadLocalProcessor) c).getThreadLocal());

    assertFalse(chainIterator.hasNext());
  }
}
