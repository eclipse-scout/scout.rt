/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.MDC;

@RunWith(PlatformTestRunner.class)
public class NamedThreadFactoryTest {

  private static final String MDC_KEY = "mdcTestKey";

  @Test
  public void testMdc() throws InterruptedException {
    MDC.put(MDC_KEY, "value should not be pushed to child thread");

    final AtomicReference<String> childThreadMdcValue = new AtomicReference<>();
    final CountDownLatch latch = new CountDownLatch(1);
    new NamedThreadFactory("mdc-test-thread").newThread(() -> {
      childThreadMdcValue.set(MDC.get(MDC_KEY));
      latch.countDown();
    }).start();

    latch.await(1, TimeUnit.SECONDS);
    assertNull(childThreadMdcValue.get());
  }
}
