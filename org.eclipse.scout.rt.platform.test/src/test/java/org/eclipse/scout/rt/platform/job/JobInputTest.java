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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobInputTest {

  @Before
  public void before() {
    NlsLocale.CURRENT.remove();
  }

  @Test
  public void testCopy() {
    JobInput input = JobInput.fillEmpty();
    input.name("name");
    input.id("123");

    JobInput copy = input.copy();

    assertNotSame(input.getRunContext(), copy.getRunContext());
    assertEquals(input.getName(), copy.getName());
    assertEquals(input.getId(), copy.getId());
  }

  @Test
  public void testFillCurrentName() {
    assertNull(JobInput.fillCurrent().getName());
    assertEquals("ABC", JobInput.fillCurrent().name("ABC").getName());
  }

  @Test
  public void testFillCurrentId() {
    assertNull(JobInput.fillCurrent().getId());
    assertEquals("123", JobInput.fillCurrent().id("123").getId());
  }
}
