/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.junit.Assert;

public class Capturer<TYPE> {

  private volatile TYPE m_message;
  private final CountDownLatch m_latch = new CountDownLatch(1);

  public void set(TYPE message) {
    m_message = message;
    m_latch.countDown();
  }

  public TYPE get() throws InterruptedException {
    return get(15, TimeUnit.SECONDS);
  }

  public TYPE get(long timeout, TimeUnit unit) throws InterruptedException {
    if (!m_latch.await(timeout, unit)) {
      throw new TimedOutError("timout elapsed while waiting for message");
    }
    return m_message;
  }

  public void assertEmpty(int timeout, TimeUnit unit) throws InterruptedException {
    try {
      TYPE result = get(timeout, unit);
      Assert.fail("Found unexpected captured value: " + result);
    }
    catch (TimedOutError e) {
      // is empty -> ok
    }
  }
}
