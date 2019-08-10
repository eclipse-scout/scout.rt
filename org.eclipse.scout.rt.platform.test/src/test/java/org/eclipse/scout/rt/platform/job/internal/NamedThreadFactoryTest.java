/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
    new NamedThreadFactory("mdc-test-thread").newThread(new Runnable() {
      @Override
      public void run() {
        childThreadMdcValue.set(MDC.get(MDC_KEY));
        latch.countDown();
      }
    }).start();

    latch.await(1, TimeUnit.SECONDS);
    assertNull(childThreadMdcValue.get());
  }
}
