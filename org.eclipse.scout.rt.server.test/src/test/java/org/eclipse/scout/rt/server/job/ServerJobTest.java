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
package org.eclipse.scout.rt.server.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ServerJobTest {

  @Mock
  private IServerSession m_serverSession1;
  @Mock
  private IServerSession m_serverSession2;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = AssertionException.class)
  public void testNoSession() throws ProcessingException {
    ISession.CURRENT.remove();
    ServerJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
      }
    }).awaitDoneAndGet();
  }

  @Test
  public void testNoSessionRequired() throws ProcessingException {
    ISession.CURRENT.remove();
    ServerJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
      }
    }, ServerJobInput.defaults().setSessionRequired(false)).awaitDoneAndGet();
  }

  @Test
  public void testThreadName() throws ProcessingException, InterruptedException {
    ISession.CURRENT.set(m_serverSession1);
    Thread.currentThread().setName("main");

    final Holder<String> actualThreadName1 = new Holder<>();
    final Holder<String> actualThreadName2 = new Holder<>();

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(2);

    ServerJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualThreadName1.setValue(Thread.currentThread().getName());
        setupLatch.countDown();

        ServerJobs.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualThreadName2.setValue(Thread.currentThread().getName());
            setupLatch.countDown();
          }
        }, ServerJobInput.defaults().setId("200").setName("XYZ"));
      }
    }, ServerJobInput.defaults().setId("100").setName("ABC"));

    assertTrue(setupLatch.await());

    assertTrue(actualThreadName1.getValue().matches("scout-server-thread-(\\d)+;100:ABC"));
    assertTrue(actualThreadName2.getValue().matches("scout-server-thread-(\\d)+;200:XYZ"));
    assertEquals("main", Thread.currentThread().getName());
  }

  @Test
  public void testJobInput() throws ProcessingException {
    ISession.CURRENT.set(m_serverSession1);

    final Holder<JobInput> jobInput = new Holder<>();

    ServerJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        jobInput.setValue(IFuture.CURRENT.get().getJobInput());
      }
    }).awaitDoneAndGet();

    assertTrue(jobInput.getValue() instanceof ServerJobInput);
  }
}
