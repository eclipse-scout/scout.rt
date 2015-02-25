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
package org.eclipse.scout.commons.job.interceptor;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.holders.StringHolder;
import org.junit.Test;

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

    // single job
    Thread.currentThread().setName("worker-1");
    new ThreadNameDecorator<Void>(next, "jobname").call();
    assertEquals("thread:worker-1;job:jobname", threadName.getValue());
    assertEquals("worker-1", Thread.currentThread().getName());

    // nested job
    Thread.currentThread().setName("thread:worker-1;job:job-1");
    new ThreadNameDecorator<Void>(next, "jobname").call();
    assertEquals("thread:worker-1;job:jobname", threadName.getValue());
    assertEquals("thread:worker-1;job:job-1", Thread.currentThread().getName());
  }
}
