/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Testcases for {@link ExceptionUtility}
 */
public class ExceptionUtilityTest {

  @Test
  public void testGetRootCause() {
    assertNull(ExceptionUtility.getRootCause(null));

    Exception e = new Exception("expected JUnit test exception");
    assertSame(e, ExceptionUtility.getRootCause(e));
    assertSame(e, ExceptionUtility.getRootCause(new Exception("expected JUnit test exception", e)));
    assertSame(e, ExceptionUtility.getRootCause(new Throwable(new Exception("expected JUnit test exception", e))));
  }

  @Test
  public void testContainsStacktrace() {
    assertFalse(ExceptionUtility.containsStacktrace(null));
    assertFalse(ExceptionUtility.containsStacktrace("foo"));
    assertFalse(ExceptionUtility.containsStacktrace("multi\nline\nfoo"));

    assertTrue(ExceptionUtility.containsStacktrace("mocked error \n at org.eclipse.scout(MockClass:1000)"));

    Exception e = new Exception("expected JUnit test exception");
    assertTrue(ExceptionUtility.containsStacktrace(ExceptionUtility.getText(e)));
  }

  @Test
  public void testGetText() {
    assertNull(ExceptionUtility.getText(null));

    Exception e1 = new Exception("expected JUnit test exception");
    e1.setStackTrace(new StackTraceElement[]{}); // strip stacktrace
    assertEquals("java.lang.Exception: expected JUnit test exception", StringUtility.removeNewLines(ExceptionUtility.getText(e1)).trim());

    Exception e2 = new Exception("expected JUnit test exception");
    e2.setStackTrace(new StackTraceElement[]{e2.getStackTrace()[0]}); // strip stacktrace to first line
    String actual = StringUtility.replaceNewLines(ExceptionUtility.getText(e2), "#");
    assertTrue(actual.startsWith("java.lang.Exception: expected JUnit test exception#\tat org.eclipse.scout.rt.platform.util.ExceptionUtilityTest.testGetText(ExceptionUtilityTest.java:")); // ignore line numbers
  }
}
