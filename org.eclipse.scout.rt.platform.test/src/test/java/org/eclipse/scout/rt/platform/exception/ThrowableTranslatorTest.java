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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class ThrowableTranslatorTest {

  @Test
  public void testException() {
    Exception e = new Exception();
    assertSame(e, new ThrowableTranslator().translate(e));
  }

  @Test
  public void testRuntimeException() {
    RuntimeException re = new RuntimeException();
    assertSame(re, new ThrowableTranslator().translate(re));
  }

  @Test
  public void testThrowable() {
    Throwable t = new Throwable();
    assertSame(t, new ThrowableTranslator().translate(t));
  }

  @Test
  public void testError() {
    Error e = new Error();
    assertSame(e, new ThrowableTranslator().translate(e));
  }

  @Test
  public void testUndeclaredThrowableException() {
    Throwable cause = new Throwable();
    UndeclaredThrowableException ute = new UndeclaredThrowableException(cause);
    assertSame(cause, new ThrowableTranslator().translate(ute));
  }

  @Test
  public void testExecutionException() {
    Throwable cause = new Throwable();
    ExecutionException ee = new ExecutionException(cause);
    assertSame(cause, new ThrowableTranslator().translate(ee));
  }

  @Test
  public void testInvocationTargetException() {
    Throwable cause = new Throwable();
    InvocationTargetException ite = new InvocationTargetException(cause);
    assertSame(cause, new ThrowableTranslator().translate(ite));
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
