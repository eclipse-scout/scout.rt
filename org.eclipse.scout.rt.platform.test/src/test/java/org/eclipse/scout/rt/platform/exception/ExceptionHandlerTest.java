/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
