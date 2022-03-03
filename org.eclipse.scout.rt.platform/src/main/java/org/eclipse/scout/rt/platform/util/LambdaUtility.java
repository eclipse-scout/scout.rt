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

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
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
    try {
      return supplier.call();
    }
    catch (Exception e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }
}
