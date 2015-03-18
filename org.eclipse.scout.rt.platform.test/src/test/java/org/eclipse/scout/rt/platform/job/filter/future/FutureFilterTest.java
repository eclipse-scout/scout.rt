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
package org.eclipse.scout.rt.platform.job.filter.future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.filter.FutureFilter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FutureFilterTest {

  @Mock
  private IFuture<Object> m_future1;
  @Mock
  private IFuture<Object> m_future2;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void test1() {
    assertFalse(new FutureFilter().accept(mock(IFuture.class)));
  }

  @Test
  public void test2() {
    assertFalse(new FutureFilter(m_future1).accept(mock(IFuture.class)));
  }

  @Test
  public void test3() {
    assertTrue(new FutureFilter(m_future1).accept(m_future1));
  }

  @Test
  public void test4() {
    assertTrue(new FutureFilter(m_future1, m_future2).accept(m_future1));
    assertTrue(new FutureFilter(m_future1, m_future2).accept(m_future2));
  }
}
