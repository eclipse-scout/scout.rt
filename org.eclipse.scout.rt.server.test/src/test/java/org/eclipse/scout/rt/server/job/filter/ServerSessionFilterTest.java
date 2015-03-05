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
package org.eclipse.scout.rt.server.job.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ServerSessionFilterTest {

  @Mock
  private IFuture<Object> m_future;
  @Mock
  private IServerSession m_session1;
  @Mock
  private IServerSession m_session2;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void test1() {
    ServerJobInput input = ServerJobInput.empty().sessionRequired(false).session(null);
    when(m_future.getJobInput()).thenReturn(input);

    assertTrue(new ServerSessionFilter(null).accept(m_future));
  }

  @Test
  public void test2() {
    ServerJobInput input = ServerJobInput.empty().sessionRequired(false).session(null);
    when(m_future.getJobInput()).thenReturn(input);

    assertFalse(new ServerSessionFilter(m_session1).accept(m_future));
  }

  @Test
  public void test3() {
    ServerJobInput input = ServerJobInput.empty().session(m_session1);
    when(m_future.getJobInput()).thenReturn(input);

    assertFalse(new ServerSessionFilter(m_session2).accept(m_future));
  }

  @Test
  public void test4() {
    ServerJobInput input = ServerJobInput.empty().session(m_session1);
    when(m_future.getJobInput()).thenReturn(input);

    assertTrue(new ServerSessionFilter(m_session1).accept(m_future));
  }
}
