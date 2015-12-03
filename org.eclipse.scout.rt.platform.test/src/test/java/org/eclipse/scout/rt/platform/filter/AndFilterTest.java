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
package org.eclipse.scout.rt.platform.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.platform.filter.AndFilter;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AndFilterTest {

  @Mock
  private IFilter<Object> m_filter1;
  @Mock
  private IFilter<Object> m_filter2;
  @Mock
  private IFilter<Object> m_filter3;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = AssertionException.class)
  public void test1() {
    new AndFilter<>(new AndFilter<>()).accept(new Object());
  }

  @Test
  public void test2() {
    when(m_filter1.accept(any())).thenReturn(true);
    assertTrue(new AndFilter<>(m_filter1).accept(new Object()));
  }

  @Test
  public void test3() {
    when(m_filter1.accept(any())).thenReturn(false);
    assertFalse(new AndFilter<>(m_filter1).accept(new Object()));
  }

  @Test
  public void test4() {
    when(m_filter1.accept(any())).thenReturn(true);
    when(m_filter2.accept(any())).thenReturn(true);
    when(m_filter3.accept(any())).thenReturn(true);
    assertTrue(new AndFilter<>(m_filter1, m_filter2, m_filter3).accept(new Object()));
  }

  @Test
  public void test5() {
    when(m_filter1.accept(any())).thenReturn(true);
    when(m_filter2.accept(any())).thenReturn(false);
    when(m_filter3.accept(any())).thenReturn(true);
    assertFalse(new AndFilter<>(m_filter1, m_filter2, m_filter3).accept(new Object()));
  }
}
