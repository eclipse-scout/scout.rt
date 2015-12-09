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

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ProcessingExceptionTranslatorTest {

  @After
  public void after() {
    IFuture.CURRENT.remove();
  }

  @Test
  public void testTranslate() {
    ProcessingExceptionTranslator exceptionTranslator = new ProcessingExceptionTranslator();

    ProcessingException pe1 = new ProcessingException();
    RuntimeException reWithPe = new RuntimeException(pe1);
    Exception e1 = new Exception();
    RuntimeException re1 = new RuntimeException();

    // test 'normal cases'
    assertSame(pe1, exceptionTranslator.translate(pe1));
    assertSame(pe1, exceptionTranslator.translate(reWithPe));
    assertSame(e1, exceptionTranslator.translate(e1).getCause());
    assertSame(re1, exceptionTranslator.translate(re1).getCause());

    // test 'Error' (1)
    Error error = new Error();
    try {
      exceptionTranslator.translate(error);
      fail("Error's should not be translated but re-throw instead");
    }
    catch (Throwable t) {
      assertSame(error, t);
    }

    // test 'Error' (2)
    AssertionError assertError = new AssertionError();
    try {
      exceptionTranslator.translate(assertError);
      fail("Error's should not be translated but re-throw instead");
    }
    catch (Throwable t) {
      assertSame(assertError, t);

    }

    // test 'UndeclaredThrowableException'
    UndeclaredThrowableException ute1 = new UndeclaredThrowableException(null);
    assertSame(ute1, exceptionTranslator.translate(ute1).getCause());

    UndeclaredThrowableException ute2 = new UndeclaredThrowableException(e1);
    assertSame(e1, exceptionTranslator.translate(ute2).getCause());

    UndeclaredThrowableException ute3 = new UndeclaredThrowableException(re1);
    assertSame(re1, exceptionTranslator.translate(ute3).getCause());

    UndeclaredThrowableException ute4 = new UndeclaredThrowableException(reWithPe);
    assertSame(reWithPe.getCause(), exceptionTranslator.translate(ute4));

    // test 'InvocationTargetException'
    InvocationTargetException ite1 = new InvocationTargetException(null);
    assertSame(ite1, exceptionTranslator.translate(ite1).getCause());

    InvocationTargetException ite2 = new InvocationTargetException(e1);
    assertSame(e1, exceptionTranslator.translate(ite2).getCause());

    InvocationTargetException ite3 = new InvocationTargetException(re1);
    assertSame(re1, exceptionTranslator.translate(ite3).getCause());

    InvocationTargetException ite4 = new InvocationTargetException(reWithPe);
    assertSame(reWithPe.getCause(), exceptionTranslator.translate(ite4));

    // test 'ExecutionException'
    ExecutionException ee1 = new ExecutionException(null);
    assertSame(ee1, exceptionTranslator.translate(ee1).getCause());

    ExecutionException ee2 = new ExecutionException(e1);
    assertSame(e1, exceptionTranslator.translate(ee2).getCause());

    ExecutionException ee3 = new ExecutionException(re1);
    assertSame(re1, exceptionTranslator.translate(ee3).getCause());

    ExecutionException ee4 = new ExecutionException(reWithPe);
    assertSame(reWithPe.getCause(), exceptionTranslator.translate(ee4));
  }
}
