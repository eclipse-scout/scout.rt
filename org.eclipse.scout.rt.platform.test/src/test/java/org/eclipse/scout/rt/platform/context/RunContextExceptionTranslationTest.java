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

import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
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

    // Test with DefaultRuntimeExceptionTranslator
    try {
      RunContexts.copyCurrent().run(runnableWithError);
      fail();
    }
    catch (PlatformException e) {
      assertSame(error, e.getCause());
    }

    // Test with DefaultRuntimeExceptionTranslator
    try {
      RunContexts.copyCurrent().run(runnableWithError, DefaultRuntimeExceptionTranslator.class);
      fail();
    }
    catch (PlatformException e) {
      assertSame(error, e.getCause());
    }

    // Test with DefaultExceptionTranslator
    try {
      RunContexts.copyCurrent().run(runnableWithError, DefaultExceptionTranslator.class);
      fail();
    }
    catch (RuntimeException e) {
      fail();
    }
    catch (Exception e) {
      assertSame(error, e);
    }

    // Test with PlatformExceptionTranslator
    try {
      RunContexts.copyCurrent().run(runnableWithError, PlatformExceptionTranslator.class);
      fail();
    }
    catch (PlatformException e) {
      assertSame(error, e.getCause());
    }
  }
}
