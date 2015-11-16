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
package org.eclipse.scout.rt.client.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ClientJobsTest {

  @Mock
  private IClientSession m_clientSession;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void after() {
    ISession.CURRENT.remove();
  }

  @Test
  public void testIsClientJob() {
    assertFalse(ClientJobs.isClientJob(null));

    IFuture<?> future = mock(IFuture.class);
    when(future.getJobInput()).thenReturn(new JobInput());
    assertFalse(ClientJobs.isClientJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().withRunContext(new RunContext()));
    assertFalse(ClientJobs.isClientJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().withRunContext(new ClientRunContext()));
    assertTrue(ClientJobs.isClientJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().withRunContext(new ClientRunContext()).withMutex(null));
    assertTrue(ClientJobs.isClientJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().withRunContext(new ClientRunContext()).withMutex(new Object()));
    assertTrue(ClientJobs.isClientJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().withRunContext(new ClientRunContext()).withMutex(mock(IClientSession.class)));
    assertFalse(ClientJobs.isClientJob(future));
  }

  @Test
  public void testScheduleWithoutInput() {
    ISession.CURRENT.set(m_clientSession);

    // Test schedule
    IFuture<?> actualFuture = ClientJobs.schedule(new Callable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }).awaitDoneAndGet();

    assertTrue(ClientJobs.isClientJob(actualFuture));
    assertFalse(ModelJobs.isModelJob(actualFuture));

    // schedule with delay
    actualFuture = ClientJobs.schedule(new Callable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }, ClientJobs.newInput(ClientRunContexts.copyCurrent())
        .withSchedulingDelay(0, TimeUnit.MILLISECONDS))
        .awaitDoneAndGet();

    assertTrue(ClientJobs.isClientJob(actualFuture));
    assertFalse(ModelJobs.isModelJob(actualFuture));

    // schedule at fixed rate
    final Holder<IFuture<?>> actualFutureHolder = new Holder<IFuture<?>>();
    ClientJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, ClientJobs.newInput(ClientRunContexts.copyCurrent())
        .withSchedulingDelay(0, TimeUnit.MILLISECONDS)
        .withPeriodicExecutionAtFixedRate(0, TimeUnit.MILLISECONDS))
        .awaitDone();

    assertTrue(ClientJobs.isClientJob(actualFutureHolder.getValue()));
    assertFalse(ModelJobs.isModelJob(actualFutureHolder.getValue()));

    // schedule with fixed delay
    actualFutureHolder.setValue(null);
    ClientJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, ClientJobs.newInput(ClientRunContexts.copyCurrent())
        .withSchedulingDelay(0, TimeUnit.MILLISECONDS)
        .withPeriodicExecutionWithFixedDelay(0, TimeUnit.MILLISECONDS))
        .awaitDone();

    assertTrue(ClientJobs.isClientJob(actualFutureHolder.getValue()));
    assertFalse(ModelJobs.isModelJob(actualFutureHolder.getValue()));
  }

  @Test
  public void testScheduleWithoutInputWithoutSession() {
    ISession.CURRENT.set(null);
    ClientJobs.schedule(mock(IRunnable.class));
    assertTrue(true); // it is valid to run a client job without session
  }

  @Test
  public void testNewInput() {
    ClientRunContext runContext = ClientRunContexts.empty();

    assertSame(runContext, ClientJobs.newInput(runContext).getRunContext());
    assertEquals("scout-client-thread", ClientJobs.newInput(runContext).getThreadName());
  }

  @Test(expected = AssertionException.class)
  public void testNewInputNullInput() {
    ClientJobs.newInput(null);
  }
}
