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
package org.eclipse.scout.commons.job.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.JobInput;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JobFilterTest {

  @Mock
  private IFuture<Object> m_future;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void test1() {
    JobInput input = JobInput.empty().id(null);
    when(m_future.getJobInput()).thenReturn(input);

    assertTrue(new JobFilter(null).accept(m_future));
  }

  @Test
  public void test2() {
    JobInput input = JobInput.empty().id(null);
    when(m_future.getJobInput()).thenReturn(input);

    assertFalse(new JobFilter("ABC").accept(m_future));
  }

  @Test
  public void test3() {
    JobInput input = JobInput.empty().id("ABC");
    when(m_future.getJobInput()).thenReturn(input);

    assertFalse(new JobFilter("abc").accept(m_future));
  }

  @Test
  public void test4() {
    JobInput input = JobInput.empty().id("ABC");
    when(m_future.getJobInput()).thenReturn(input);

    assertTrue(new JobFilter("ABC").accept(m_future));
  }
}
