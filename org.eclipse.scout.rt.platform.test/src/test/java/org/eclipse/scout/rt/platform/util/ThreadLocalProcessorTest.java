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

import static org.junit.Assert.assertEquals;

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
    callableChain.call(() -> {
      actualValue.setValue(THREAD_LOCAL.get());
      return null;
    });

    assertEquals("ABC", actualValue.getValue());
    assertEquals("ORIG", THREAD_LOCAL.get());
  }
}
