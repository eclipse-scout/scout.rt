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
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.junit.Test;

public class ThreadLocalProcessorTest {

  private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

  @Test
  public void test() throws Exception {
    final StringHolder actualValue = new StringHolder();

    THREAD_LOCAL.set("ORIG");

    CallableChain<Void> callableChain = new CallableChain<>();
    callableChain.add(new ThreadLocalProcessor<>(THREAD_LOCAL, "ABC"));
    callableChain.call(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        actualValue.setValue(THREAD_LOCAL.get());
        return null;
      }
    });

    assertEquals("ABC", actualValue.getValue());
    assertEquals("ORIG", THREAD_LOCAL.get());
  }
}
