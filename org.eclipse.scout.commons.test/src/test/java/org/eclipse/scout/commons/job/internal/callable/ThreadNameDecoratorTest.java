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
import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.JobInput;
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

    Thread.currentThread().setName("worker-1");

    IJobInput input = JobInput.empty().id("123").name("job1");

    new ThreadNameDecorator<Void>(next, input).call();
    assertEquals("worker-1;123[job1]", threadName.getValue());
    assertEquals("worker-1", Thread.currentThread().getName());
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

    Thread.currentThread().setName("worker-1");

    new ThreadNameDecorator<Void>(next, JobInput.empty()).call();
    assertEquals("worker-1", threadName.getValue());
    assertEquals("worker-1", Thread.currentThread().getName());
  }

  @Test
  public void testNestedJob1() throws Exception {
    final StringHolder threadName = new StringHolder();

    Callable<Void> next = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        threadName.setValue(Thread.currentThread().getName());
        return null;
      }
    };

    Thread.currentThread().setName("worker-1;job1");

    IJobInput input = JobInput.empty().id("123").name("job2");

    // nested job
    new ThreadNameDecorator<Void>(next, input).call();
    assertEquals("worker-1;job1;123[job2]", threadName.getValue());
    assertEquals("worker-1;job1", Thread.currentThread().getName());
  }

  @Test
  public void testNestedJob2() throws Exception {
    final StringHolder threadName = new StringHolder();

    Callable<Void> next = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        threadName.setValue(Thread.currentThread().getName());
        return null;
      }
    };

    Thread.currentThread().setName("worker-1;2[job1]");

    IJobInput input = JobInput.empty().id("123").name("job2");

    // nested job
    new ThreadNameDecorator<Void>(next, input).call();
    assertEquals("worker-1;2[job1];123[job2]", threadName.getValue());
    assertEquals("worker-1;2[job1]", Thread.currentThread().getName());
  }

  @Test
  public void testNestedJobWithoutIdentifier() throws Exception {
    final StringHolder threadName = new StringHolder();

    Callable<Void> next = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        threadName.setValue(Thread.currentThread().getName());
        return null;
      }
    };

    Thread.currentThread().setName("worker-1;2[ABC]");

    // nested job
    new ThreadNameDecorator<Void>(next, JobInput.empty()).call();
    assertEquals("worker-1;2[ABC]", threadName.getValue());
    assertEquals("worker-1;2[ABC]", Thread.currentThread().getName());
  }

}
