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
package org.eclipse.scout.rt.platform.exception;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class ExceptionTranslatorTest {

  @Test
  public void testException() {
    Exception e = new Exception();
    assertSame(e, new ExceptionTranslator().translate(e));
  }

  @Test
  public void testRuntimeException() {
    RuntimeException re = new RuntimeException();
    assertSame(re, new ExceptionTranslator().translate(re));
  }

  @Test
  public void testThrowable() {
    Throwable t = new Throwable();
    assertSame(t, new ExceptionTranslator().translate(t).getCause());
  }

  @Test
  public void testError() {
    Error e = new Error();
    try {
      new ExceptionTranslator().translate(e);
      fail("error is expected to be re-thrown");
    }
    catch (Error actualError) {
      assertSame(e, actualError);
    }
  }

  @Test
  public void testUndeclaredThrowableException() {
    Exception cause = new Exception();
    UndeclaredThrowableException ute = new UndeclaredThrowableException(cause);
    assertSame(cause, new ExceptionTranslator().translate(ute));
  }

  @Test
  public void testExecutionException() {
    Exception cause = new Exception();
    ExecutionException ee = new ExecutionException(cause);
    assertSame(cause, new ExceptionTranslator().translate(ee));
  }

  @Test
  public void testInvocationTargetException() {
    Exception cause = new Exception();
    InvocationTargetException ite = new InvocationTargetException(cause);
    assertSame(cause, new ExceptionTranslator().translate(ite));
  }

  @Test
  public void testUndeclaredThrowableWithNullCauseException() {
    UndeclaredThrowableException ute = new UndeclaredThrowableException(null);
    assertSame(ute, new ExceptionTranslator().translate(ute));
  }

  @Test
  public void testExecutionExceptionWithNullCause() {
    ExecutionException ee = new ExecutionException(null);
    assertSame(ee, new ExceptionTranslator().translate(ee));
  }

  @Test
  public void testInvocationTargetExceptionWithNullCause() {
    InvocationTargetException ite = new InvocationTargetException(null);
    assertSame(ite, new ExceptionTranslator().translate(ite));
  }
}
