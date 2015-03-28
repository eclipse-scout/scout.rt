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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ServerJobsTest {

  @Mock
  private IServerSession m_serverSession;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testIsServerJob() {
    assertFalse(ServerJobs.isServerJob(null));

    IFuture<?> future = mock(IFuture.class);
    when(future.getJobInput()).thenReturn(new JobInput());
    assertFalse(ServerJobs.isServerJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new RunContext()));
    assertFalse(ServerJobs.isServerJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new ServerRunContext()));
    assertTrue(ServerJobs.isServerJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new ServerRunContext()).mutex(null));
    assertTrue(ServerJobs.isServerJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new ServerRunContext()).mutex(new Object()));
    assertTrue(ServerJobs.isServerJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new ServerRunContext()).mutex(mock(IServerSession.class)));
    assertTrue(ServerJobs.isServerJob(future));
  }

  @Test
  public void testScheduleWithoutInput() throws ProcessingException {
    ISession.CURRENT.set(m_serverSession);

    // Test schedule
    IFuture<?> actualFuture = ServerJobs.schedule(new ICallable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }).awaitDoneAndGet();

    assertTrue(ServerJobs.isServerJob(actualFuture));

    // schedule with delay
    actualFuture = ServerJobs.schedule(new ICallable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }, 0, TimeUnit.MILLISECONDS).awaitDoneAndGet();

    assertTrue(ServerJobs.isServerJob(actualFuture));

    // schedule at fixed rate
    final Holder<IFuture<?>> actualFutureHolder = new Holder<IFuture<?>>();
    ServerJobs.scheduleAtFixedRate(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, 0, 0, TimeUnit.MILLISECONDS, ServerJobs.newInput(ServerRunContexts.copyCurrent())).awaitDoneAndGet();

    assertTrue(ServerJobs.isServerJob(actualFutureHolder.getValue()));

    // schedule with fixed delay
    actualFutureHolder.setValue(null);
    ServerJobs.scheduleWithFixedDelay(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, 0, 0, TimeUnit.MILLISECONDS, ServerJobs.newInput(ServerRunContexts.copyCurrent())).awaitDoneAndGet();

    assertTrue(ServerJobs.isServerJob(actualFutureHolder.getValue()));
  }

  @Test
  public void testScheduleWithoutInputWithoutSession() throws ProcessingException {
    ISession.CURRENT.set(null);
    ServerJobs.schedule(mock(IRunnable.class));
  }

  @Test
  public void testNewInput() {
    ServerRunContext runContext = ServerRunContexts.empty();

    assertSame(runContext, ServerJobs.newInput(runContext).runContext());
    assertEquals("scout-server-thread", ServerJobs.newInput(runContext).threadName());
  }

  @Test(expected = AssertionException.class)
  public void testNewInputNullInput() {
    ServerJobs.newInput(null);
  }
}
