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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IThrowableTranslator;
import org.eclipse.scout.rt.platform.exception.RuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JobExceptionTranslationTest {

  private IBean<Object> m_bean;

  @Before
  public void before() {
    m_bean = Platform.get().getBeanManager().registerBean(new BeanMetaData(JobManager.class, new JobManager()).withReplace(true).withOrder(-1));
  }

  @After
  public void after() {
    Jobs.getJobManager().shutdown();
    Platform.get().getBeanManager().unregisterBean(m_bean);
  }

  @Test
  public void testWithoutRunContext() {
    final Exception error = new Exception();

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw error;
      }
    }, Jobs.newInput());

    // Test with default translator (ProcessingExceptionTranslator)
    try {
      future.awaitDoneAndGet();
      fail();
    }
    catch (ProcessingException e) {
      assertSame(error, e.getCause());
    }

    // Test with default translator (ExceptionTranslator)
    try {
      future.awaitDoneAndGet(BEANS.get(ExceptionTranslator.class));
      fail();
    }
    catch (Exception e) {
      assertSame(error, e);
    }

    // Test with default translator (RuntimeExceptionTranslator)
    try {
      future.awaitDoneAndGet(BEANS.get(RuntimeExceptionTranslator.class));
      fail();
    }
    catch (RuntimeException e) {
      assertSame(error, e.getCause());
    }

    future.awaitDoneAndGet(new IThrowableTranslator<RuntimeException>() {

      @Override
      public RuntimeException translate(Throwable t) {
        return null; // null=swallow
      }
    });
  }

  @Test
  public void testWithRunContext() {
    final Exception error = new Exception();

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw error;
      }
    }, Jobs.newInput().withRunContext(RunContexts.copyCurrent()));

    // Test with default translator (ProcessingExceptionTranslator)
    try {
      future.awaitDoneAndGet();
      fail();
    }
    catch (ProcessingException e) {
      assertSame(error, e.getCause());
    }

    // Test with default translator (ExceptionTranslator)
    try {
      future.awaitDoneAndGet(BEANS.get(ExceptionTranslator.class));
      fail();
    }
    catch (Exception e) {
      assertSame(error, e);
    }

    // Test with default translator (RuntimeExceptionTranslator)
    try {
      future.awaitDoneAndGet(BEANS.get(RuntimeExceptionTranslator.class));
      fail();
    }
    catch (RuntimeException e) {
      assertSame(error, e.getCause());
    }

    // Test with 'swallowed' exception.
    future.awaitDoneAndGet(new IThrowableTranslator<RuntimeException>() {

      @Override
      public RuntimeException translate(Throwable t) {
        return null; // null=swallow
      }
    });
  }
}
