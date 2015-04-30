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

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
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

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new RunContext()));
    assertFalse(ClientJobs.isClientJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new ClientRunContext()));
    assertTrue(ClientJobs.isClientJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new ClientRunContext()).mutex(null));
    assertTrue(ClientJobs.isClientJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new ClientRunContext()).mutex(new Object()));
    assertTrue(ClientJobs.isClientJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new ClientRunContext()).mutex(mock(IClientSession.class)));
    assertFalse(ClientJobs.isClientJob(future));
  }

  @Test
  public void testScheduleWithoutInput() throws ProcessingException {
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
    }, 0, TimeUnit.MILLISECONDS).awaitDoneAndGet();

    assertTrue(ClientJobs.isClientJob(actualFuture));
    assertFalse(ModelJobs.isModelJob(actualFuture));

    // schedule at fixed rate
    final Holder<IFuture<?>> actualFutureHolder = new Holder<IFuture<?>>();
    ClientJobs.scheduleAtFixedRate(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, 0, 0, TimeUnit.MILLISECONDS, ClientJobs.newInput(ClientRunContexts.copyCurrent())).awaitDoneAndGet();

    assertTrue(ClientJobs.isClientJob(actualFutureHolder.getValue()));
    assertFalse(ModelJobs.isModelJob(actualFutureHolder.getValue()));

    // schedule with fixed delay
    actualFutureHolder.setValue(null);
    ClientJobs.scheduleWithFixedDelay(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, 0, 0, TimeUnit.MILLISECONDS, ClientJobs.newInput(ClientRunContexts.copyCurrent())).awaitDoneAndGet();

    assertTrue(ClientJobs.isClientJob(actualFutureHolder.getValue()));
    assertFalse(ModelJobs.isModelJob(actualFutureHolder.getValue()));
  }

  @Test(expected = AssertionError.class)
  public void testScheduleWithoutInputWithoutSession() throws ProcessingException {
    ISession.CURRENT.set(null);
    ClientJobs.schedule(mock(IRunnable.class));
  }

  @Test
  public void testNewInput() {
    ClientRunContext runContext = ClientRunContexts.empty();

    assertSame(runContext, ClientJobs.newInput(runContext).runContext());
    assertEquals("scout-client-thread", ClientJobs.newInput(runContext).threadName());
  }

  @Test(expected = AssertionError.class)
  public void testNewInputNullInput() {
    ClientJobs.newInput(null);
  }
}
