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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
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
    assertNull(ModelJobInput.empty().getMutex());
  }

  @Test
  public void testMutex2() {
    IClientSession session = mock(IClientSession.class);
    ISession.CURRENT.set(session);
    assertSame(session, ModelJobInput.defaults().getMutex());
  }

  @Test
  public void testMutex3() {
    IClientSession session1 = mock(IClientSession.class);
    IClientSession session2 = mock(IClientSession.class);
    ISession.CURRENT.set(session1);
    assertSame(session2, ModelJobInput.defaults().session(session2).getMutex());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testMutex4() {
    ModelJobInput.defaults().mutex(new Object());
  }
}
