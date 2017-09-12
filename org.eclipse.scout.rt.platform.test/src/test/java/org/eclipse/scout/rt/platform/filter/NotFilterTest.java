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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NotFilterTest {

  @Mock
  private IFilter<Object> m_filter;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = AssertionException.class)
  public void test1() {
    new NotFilter<>(new NotFilter<>(null)).accept(new Object());
  }

  @Test
  public void test2() {
    when(m_filter.accept(any())).thenReturn(false);
    assertTrue(new NotFilter<>(m_filter).accept(new Object()));
  }

  @Test
  public void test3() {
    when(m_filter.accept(any())).thenReturn(true);
    assertFalse(new NotFilter<>(m_filter).accept(new Object()));
  }
}
