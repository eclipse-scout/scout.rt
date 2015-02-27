/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.job.internal.callable;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.holders.StringHolder;
import org.junit.Test;

public class InitThreadLocalCallableTest {

  private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

  @Test
  public void test() throws Exception {
    final StringHolder actualValue = new StringHolder();

    Callable<Void> next = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        actualValue.setValue(THREAD_LOCAL.get());
        return null;
      }
    };

    THREAD_LOCAL.set("ORIG");

    new InitThreadLocalCallable<Void, String>(next, THREAD_LOCAL, "ABC").call();
    assertEquals("ABC", actualValue.getValue());
    assertEquals("ORIG", THREAD_LOCAL.get());
  }
}
