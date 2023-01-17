/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.NullExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
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

    IFuture<Void> future = Jobs.schedule((IRunnable) () -> {
      throw error;
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

    IFuture<Void> future = Jobs.schedule((IRunnable) () -> {
      throw error;
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
    final FutureCancelledError cancellationException = new FutureCancelledError("expected JUnit test exception");

    IFuture<Void> future = Jobs.schedule((IRunnable) () -> {
      throw cancellationException;
    }, Jobs.newInput());

    try {
      future.awaitDoneAndGet(NullExceptionTranslator.class);
      fail("ExecutionException expected");
    }
    catch (FutureCancelledError e) {
      fail("ExecutionException expected");
    }
    catch (ExecutionException e) {
      assertFalse(future.isCancelled());
      assertSame(cancellationException, e.getCause());
    }
  }
}
