/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.context;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.junit.Test;

public class RunContextExceptionTranslationTest {

  @Test
  public void test() {
    final Exception error = new Exception("expected JUnit test exception");

    IRunnable runnableWithError = () -> {
      throw error;
    };

    // Test with DefaultRuntimeExceptionTranslator
    try {
      RunContexts.empty().run(runnableWithError);
      fail();
    }
    catch (PlatformException e) {
      assertSame(error, e.getCause());
    }

    // Test with DefaultRuntimeExceptionTranslator
    try {
      RunContexts.empty().run(runnableWithError, DefaultRuntimeExceptionTranslator.class);
      fail();
    }
    catch (PlatformException e) {
      assertSame(error, e.getCause());
    }

    // Test with DefaultExceptionTranslator
    try {
      RunContexts.empty().run(runnableWithError, DefaultExceptionTranslator.class);
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
      RunContexts.empty().run(runnableWithError, PlatformExceptionTranslator.class);
      fail();
    }
    catch (PlatformException e) {
      assertSame(error, e.getCause());
    }
  }
}
