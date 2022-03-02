/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;

public final class LambdaUtility {

  private LambdaUtility() {
    // static access only
  }

  public static void invokeSafely(UnsafeRunnable runnable) {
    invokeSafely(() -> {
      runnable.run();
      return null;
    });
  }

  public static <T> T invokeSafely(UnsafeSupplier<T> supplier) {
    try {
      return supplier.get();
    }
    catch (Exception e) {
      throw BEANS.get(ExceptionHandler.class).convertAsRuntimeException(e);
    }
  }

  @FunctionalInterface
  public interface UnsafeRunnable {
    void run() throws Exception;
  }

  @FunctionalInterface
  public interface UnsafeSupplier<T> {
    T get() throws Exception;
  }
}
