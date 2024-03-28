/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.testing.platform.runner.statement;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

@RunWith(PlatformTestRunner.class)
public class TimeoutRunContextStatementTest {

  @Test
  public void testTestTimedOutException() {
    Assert.assertThrows(TestTimedOutException.class, () -> new TimeoutRunContextStatement(new Statement() {
      @Override
      public void evaluate() {
        SleepUtil.sleepSafe(5L, TimeUnit.SECONDS);
      }
    }, TimeUnit.SECONDS.toMillis(1)).evaluate());
  }

  @Test
  public void testOriginalExceptionIsThrownEvenOnTimeout() {
    Assert.assertThrows(InterruptedException.class, () -> new TimeoutRunContextStatement(new Statement() {
      @Override
      public void evaluate() throws InterruptedException {
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
      }
    }, TimeUnit.SECONDS.toMillis(1)).evaluate());
  }
}
