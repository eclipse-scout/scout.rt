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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.NullExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobExceptionTranslationTest {

  @Before
  public void before() {
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));
  }

  @Test
  public void testWithImplicitExceptionTranslator() {
    final Exception error = new Exception("expected JUnit test exception");

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw error;
      }
    }, Jobs.newInput());

    try {
      future.awaitDoneAndGet();
      fail("PlatformException expected");
    }
    catch (PlatformException e) {
      assertSame(error, e.getCause());
    }
  }

  @Test
  public void testWithExplicitExceptionTranslator() {
    final Exception error = new Exception("expected JUnit test exception");

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw error;
      }
    }, Jobs.newInput());

    try {
      future.awaitDoneAndGet(DefaultExceptionTranslator.class);
      fail("Exception expected");
    }
    catch (Exception e) {
      assertSame(error, e);
    }

    try {
      future.awaitDoneAndGet(DefaultRuntimeExceptionTranslator.class);
      fail("PlatformException expected");
    }
    catch (PlatformException e) {
      assertSame(error, e.getCause());
    }
  }

  @Test
  public void testWithNullExceptionTranslator() throws Throwable {
    final FutureCancelledException cancellationException = new FutureCancelledException("expected JUnit test exception");

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw cancellationException;
      }
    }, Jobs.newInput());

    try {
      future.awaitDoneAndGet(NullExceptionTranslator.class);
      fail("ExecutionException expected");
    }
    catch (FutureCancelledException e) {
      fail("ExecutionException expected");
    }
    catch (ExecutionException e) {
      assertFalse(future.isCancelled());
      assertSame(cancellationException, e.getCause());
    }
  }
}
