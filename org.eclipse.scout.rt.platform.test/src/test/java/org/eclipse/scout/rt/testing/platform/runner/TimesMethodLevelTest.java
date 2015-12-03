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
package org.eclipse.scout.rt.testing.platform.runner;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class TimesMethodLevelTest {

  private static final List<String> s_protocol = Collections.synchronizedList(new ArrayList<String>());

  @BeforeClass
  public static void beforeClass() {
    s_protocol.add("beforeClass");
  }

  @AfterClass
  public static void afterClass() {
    s_protocol.add("afterClass");

    List<String> expected = new ArrayList<>();
    expected.add("beforeClass");

    // 1st time
    expected.add("before");
    expected.add("test");
    expected.add("after");

    // 2nd time
    expected.add("before");
    expected.add("test");
    expected.add("after");

    // 3th time
    expected.add("before");
    expected.add("test");
    expected.add("after");

    expected.add("afterClass");

    assertEquals(expected, s_protocol);
  }

  @Before
  public void before() {
    s_protocol.add("before");
  }

  @After
  public void after() {
    s_protocol.add("after");
  }

  @Test
  @Times(3)
  public void test() {
    s_protocol.add("test");
  }
}
