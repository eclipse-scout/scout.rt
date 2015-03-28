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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class JobIdFilterTest {

  @Mock
  private IFuture<Object> m_future;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void test1() {
    JobInput input = Jobs.newInput(RunContexts.empty()).id(null);
    when(m_future.getJobInput()).thenReturn(input);

    assertTrue(Jobs.newFutureFilter().ids(new String[]{null}).accept(m_future));
  }

  @Test
  public void test2() {
    JobInput input = Jobs.newInput(RunContexts.empty()).id(null);
    when(m_future.getJobInput()).thenReturn(input);

    assertFalse(Jobs.newFutureFilter().ids("ABC").accept(m_future));
  }

  @Test
  public void test3() {
    JobInput input = Jobs.newInput(RunContexts.empty()).id("ABC");
    when(m_future.getJobInput()).thenReturn(input);

    assertFalse(Jobs.newFutureFilter().ids("abc").accept(m_future));
  }

  @Test
  public void test4() {
    JobInput input = Jobs.newInput(RunContexts.empty()).id("ABC");
    when(m_future.getJobInput()).thenReturn(input);

    assertTrue(Jobs.newFutureFilter().ids("ABC").accept(m_future));
  }

  @Test
  public void test5() {
    JobInput input = Jobs.newInput(RunContexts.empty()).id("XYZ");
    when(m_future.getJobInput()).thenReturn(input);

    assertTrue(Jobs.newFutureFilter().ids("ABC", "XYZ").accept(m_future));
  }
}
