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

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class NullExceptionTranslatorTest {

  @Test
  public void testTranslate() {
    NullExceptionTranslator translator = new NullExceptionTranslator();

    // Throwable
    Throwable throwable = new Throwable("expected JUnit test exception");
    assertSame(throwable, translator.translate(throwable));

    // Exception
    Exception exception = new Exception("expected JUnit test exception");
    assertSame(exception, translator.translate(exception));

    // Error
    Error error = new Error("expected JUnit test exception");
    assertSame(error, translator.translate(error));
    assertSame(error, translator.translate(error, true));
    assertSame(error, translator.translate(error, false));

    // UndeclaredThrowableException
    UndeclaredThrowableException ute = new UndeclaredThrowableException(throwable, "expected JUnit test exception");
    assertSame(ute, translator.translate(ute));

    // InvocationTargetException
    InvocationTargetException ite = new InvocationTargetException(throwable, "expected JUnit test exception");
    assertSame(ite, translator.translate(ite));

    // ExecutionException
    ExecutionException ee = new ExecutionException("expected JUnit test exception", throwable);
    assertSame(ee, translator.translate(ee));
  }
}
