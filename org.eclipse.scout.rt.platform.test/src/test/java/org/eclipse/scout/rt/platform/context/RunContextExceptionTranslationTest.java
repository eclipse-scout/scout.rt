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
package org.eclipse.scout.rt.platform.context;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IThrowableTranslator;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.RuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.junit.Test;

public class RunContextExceptionTranslationTest {

  @Test
  public void test() {
    final Exception error = new Exception();

    IRunnable runnableWithError = new IRunnable() {

      @Override
      public void run() throws Exception {
        throw error;
      }
    };

    // Test with default translator (ProcessingExceptionTranslator)
    try {
      RunContexts.copyCurrent().run(runnableWithError);
      fail();
    }
    catch (ProcessingException e) {
      assertSame(error, e.getCause());
    }

    // Test with default translator (ExceptionTranslator)
    try {
      RunContexts.copyCurrent().run(runnableWithError, BEANS.get(ExceptionTranslator.class));
      fail();
    }
    catch (Exception e) {
      assertSame(error, e);
    }

    // Test with default translator (RuntimeExceptionTranslator)
    try {
      RunContexts.copyCurrent().run(runnableWithError, BEANS.get(RuntimeExceptionTranslator.class));
      fail();
    }
    catch (RuntimeException e) {
      assertSame(error, e.getCause());
    }

    // Test with 'swallowed' exception.
    RunContexts.copyCurrent().run(runnableWithError, new IThrowableTranslator<RuntimeException>() {

      @Override
      public RuntimeException translate(Throwable t) {
        return null; // null=swallow
      }
    });
  }
}
