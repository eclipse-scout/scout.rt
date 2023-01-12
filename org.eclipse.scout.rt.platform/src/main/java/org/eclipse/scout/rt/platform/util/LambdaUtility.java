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

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

public final class LambdaUtility {

  private LambdaUtility() {
    // static access only
  }

  public static void invokeSafely(IRunnable runnable) {
    invokeSafely(() -> {
      runnable.run();
      return null;
    });
  }

  public static <T> T invokeSafely(Callable<T> supplier) {
    return invokeSafely(supplier, BEANS.get(DefaultRuntimeExceptionTranslator.class));
  }

  public static void invokeSafely(IRunnable runnable, IExceptionTranslator<RuntimeException> translator) {
    invokeSafely(() -> {
      runnable.run();
      return null;
    }, translator);
  }

  public static <T> T invokeSafely(Callable<T> supplier, IExceptionTranslator<RuntimeException> translator) {
    try {
      return supplier.call();
    }
    catch (Exception e) {
      throw translator.translate(e);
    }
  }
}
