/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
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
    JobInput input = Jobs.newInput().withRunContext(RunContexts.empty());
    input.withName("name");

    JobInput copy = input.copy();

    assertNotSame(input.getRunContext(), copy.getRunContext());
    assertEquals(input.getName(), copy.getName());
  }

  @Test
  public void testFillCurrentName() {
    assertNull(Jobs.newInput().withRunContext(RunContexts.copyCurrent()).getName());
    assertEquals("ABC", Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName("ABC").getName());
  }
}
