/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.exception;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

public class ExceptionHandlerTest {

  @Test
  public void testRootCause() {
    assertNull(ExceptionHandler.getRootCause(null));

    Exception e = new Exception("expected JUnit test exception");
    assertSame(e, ExceptionHandler.getRootCause(e));
    assertSame(e, ExceptionHandler.getRootCause(new Exception("expected JUnit test exception", e)));
    assertSame(e, ExceptionHandler.getRootCause(new Throwable(new Exception("expected JUnit test exception", e))));
  }

  @Test
  public void testToLogArguments() {
    ExceptionHandler handler = BEANS.get(ExceptionHandler.class);

    Throwable t1 = new Throwable("foo");
    assertArrayEquals(new Object[]{"Throwable", "foo", t1}, handler.toLogArguments(t1));

    Throwable t2 = new Throwable("");
    assertArrayEquals(new Object[]{"Throwable", "", t2}, handler.toLogArguments(t2));

    Throwable t3 = new Throwable((String) null);
    assertArrayEquals(new Object[]{"Throwable", "n/a", t3}, handler.toLogArguments(t3));

    Throwable t4 = new Throwable();
    assertArrayEquals(new Object[]{"Throwable", "n/a", t4}, handler.toLogArguments(t4));

    Throwable t5 = new NullPointerException("bar");
    assertArrayEquals(new Object[]{"NullPointerException", "bar", t5}, handler.toLogArguments(t5));
  }
}
