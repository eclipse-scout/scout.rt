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
package org.eclipse.scout.rt.platform.job.internal.callable;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.holders.StringHolder;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ThreadNameDecoratorTest {

  @Test
  public void test() throws Exception {
    final StringHolder threadName = new StringHolder();

    Callable<Void> next = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        threadName.setValue(Thread.currentThread().getName());
        return null;
      }
    };

    Thread.currentThread().setName("scout-thread-5 (idle)");

    JobInput input = JobInput.empty().id("123").name("job1");

    new ThreadNameDecorator<Void>(next, "scout-client-thread", input.getIdentifier()).call();
    assertEquals("scout-client-thread-5;123:job1", threadName.getValue());
    assertEquals("scout-thread-5 (idle)", Thread.currentThread().getName());
  }

  @Test
  public void testJobWithoutIdentifier() throws Exception {
    final StringHolder threadName = new StringHolder();

    Callable<Void> next = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        threadName.setValue(Thread.currentThread().getName());
        return null;
      }
    };

    Thread.currentThread().setName("scout-thread-5 (idle)");

    JobInput input = JobInput.empty();

    new ThreadNameDecorator<Void>(next, "scout-client-thread", input.getIdentifier()).call();
    assertEquals("scout-client-thread-5", threadName.getValue());
    assertEquals("scout-thread-5 (idle)", Thread.currentThread().getName());
  }
}
