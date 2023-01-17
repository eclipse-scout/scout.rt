/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.filter;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AndFilterTest {

  @Mock
  private Predicate<Object> m_filter1;
  @Mock
  private Predicate<Object> m_filter2;
  @Mock
  private Predicate<Object> m_filter3;

  @Before
  public void before() throws Exception {
    MockitoAnnotations.openMocks(this).close();
  }

  @Test(expected = AssertionException.class)
  public void test1() {
    new AndFilter<>(new AndFilter<>()).test(new Object());
  }

  @Test
  public void test2() {
    when(m_filter1.test(any())).thenReturn(true);
    assertTrue(new AndFilter<>(m_filter1).test(new Object()));
  }

  @Test
  public void test3() {
    when(m_filter1.test(any())).thenReturn(false);
    assertFalse(new AndFilter<>(m_filter1).test(new Object()));
  }

  @Test
  public void test4() {
    when(m_filter1.test(any())).thenReturn(true);
    when(m_filter2.test(any())).thenReturn(true);
    when(m_filter3.test(any())).thenReturn(true);
    assertTrue(new AndFilter<>(m_filter1, m_filter2, m_filter3).test(new Object()));
  }

  @Test
  public void test5() {
    when(m_filter1.test(any())).thenReturn(true);
    when(m_filter2.test(any())).thenReturn(false);
    when(m_filter3.test(any())).thenReturn(true);
    assertFalse(new AndFilter<>(m_filter1, m_filter2, m_filter3).test(new Object()));
  }
}
