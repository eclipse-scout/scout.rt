/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
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
  }

  @After
  public void after() {
    ISession.CURRENT.remove();
  }

  @Test
  public void testEmpty() {
    ServerRunContext runContext = ServerRunContexts.empty();
    assertNull(runContext.getSubject());
    assertNull(runContext.getSession());
    assertNull(runContext.getUserAgent());
    assertNull(runContext.getLocale());
    assertEquals(TransactionScope.REQUIRES_NEW, runContext.getTransactionScope());
  }

  @Test
  public void testCopy() {
    ServerRunContext runContext = ServerRunContexts.empty();
    runContext.getPropertyMap().put("A", "B");
    runContext.withSubject(new Subject());
    runContext.withSession(mock(IServerSession.class));
    runContext.withUserAgent(UserAgents.create().build());
    runContext.withLocale(Locale.CANADA_FRENCH);
    runContext.withTransactionScope(TransactionScope.MANDATORY);

    ServerRunContext copy = runContext.copy();

    assertEquals(toSet(runContext.getPropertyMap().iterator()), toSet(copy.getPropertyMap().iterator()));
    assertSame(runContext.getSubject(), copy.getSubject());
    assertSame(runContext.getUserAgent(), copy.getUserAgent());
    assertSame(runContext.getLocale(), copy.getLocale());
    assertEquals(TransactionScope.MANDATORY, runContext.getTransactionScope());
  }

  @Test
  public void testCurrentTransactionScope() {
    assertEquals(TransactionScope.REQUIRES_NEW, ServerRunContexts.copyCurrent().getTransactionScope());
  }

  private static Set<Object> toSet(Iterator<?> iterator) {
    Set<Object> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }
}
