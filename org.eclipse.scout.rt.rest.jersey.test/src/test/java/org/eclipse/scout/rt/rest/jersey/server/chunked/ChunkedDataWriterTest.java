/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.server.chunked;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.ws.rs.container.ConnectionCallback;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts.RunContextFactory;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.TransactionCancelledError;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.rest.chunked.IChunkedDataWriter;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.process.internal.RequestContext;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.server.ChunkedOutput;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ChunkedDataWriterTest {

  private FixtureIterator m_iterator;

  @Test
  public void testToResponse() {
    @SuppressWarnings("resource")
    IChunkedDataWriter<String> writer = IChunkedDataWriter.create(String.class, "\r\n", 100);
    Response res = writer.toResponse(() -> fetchData());
    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
    assertTrue(m_iterator.waitForCompletionAndVerifyTxn());
  }

  @Test
  public void testToResponse_supplierThrowsUncheckedException() throws Exception {
    @SuppressWarnings("resource")
    IChunkedDataWriter<String> writer = IChunkedDataWriter.create(String.class, "\r\n", 100);

    JUnitExceptionHandler exceptionHandler = BEANS.get(JUnitExceptionHandler.class);
    exceptionHandler.ignoreExceptionOnce(RuntimeException.class, () -> {
      assertThrows(RuntimeException.class,
          () -> writer.toResponse(() -> {
            throw new RuntimeException("by intention");
          }));
      // The exception above is thrown in a job and finally handled by the JUnitExceptionHandler.
      // But because it involves another job, we must give it some time to be processed.
      SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);
    });

    exceptionHandler.ignoreExceptionOnce(TransactionCancelledError.class, () -> {
      assertThrows(TransactionCancelledError.class,
          () -> writer.toResponse(() -> {
            throw new TransactionCancelledError("by intention");
          }));
      // same applies here (see comment above)
      SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);
    });
  }

  @Test
  public void testToResponse_differentTransactions() {
    IBean<Object> fixtureRunCtxFactoryBean = BeanTestingHelper.get().registerBean(new BeanMetaData(RequiresNewFixtureRunContextFactory.class));
    try {
      @SuppressWarnings("resource")
      IChunkedDataWriter<String> writer = IChunkedDataWriter.create(String.class, "\r\n", 100);
      FixtureIterator iterator = fetchData();
      Response res = writer.toResponse(() -> iterator);
      assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
      assertFalse(iterator.waitForCompletionAndVerifyTxn());
    }
    finally {
      BeanTestingHelper.get().unregisterBean(fixtureRunCtxFactoryBean);
    }
  }

  @Test
  public void testCloseWaitsForContainerFlush() throws Exception {
    @SuppressWarnings("resource")
    ChunkedDataWriter<String> writer = new ChunkedDataWriter<>();
    writer.init(String.class, "\r\n", 100);
    writer.write("a");
    writer.write("b");
    writer.write("c");

    CountDownLatch flushStarted = new CountDownLatch(1);
    CountDownLatch continueFlush = new CountDownLatch(1);
    ByteArrayOutputStream received = new ByteArrayOutputStream();
    OutputStream entityStream = new OutputStream() {
      private boolean m_firstWrite = true;

      @Override
      public void write(int b) {
        received.write(b);
      }

      @Override
      public void write(byte[] b, int off, int len) {
        if (m_firstWrite) {
          m_firstWrite = false;
          flushStarted.countDown();
          await(continueFlush);
        }
        received.write(b, off, len);
      }
    };

    RequestScope requestScope = mock(RequestScope.class);
    doAnswer(invocation -> ((Callable<?>) invocation.getArgument(1)).call())
        .when(requestScope).runInScope(any(RequestContext.class), (Callable<?>) any(Callable.class));
    ContainerRequest requestContext = mock(ContainerRequest.class);
    MessageBodyWorkers workers = mock(MessageBodyWorkers.class);
    when(requestContext.getWorkers()).thenReturn(workers);
    doAnswer(invocation -> {
      String chunk = invocation.getArgument(0);
      OutputStream output = invocation.getArgument(7);
      output.write(chunk.getBytes(StandardCharsets.UTF_8));
      return output;
    }).when(workers).writeTo(any(), any(), any(), any(), any(), any(), any(), any(), any());
    ContainerResponse responseContext = mock(ContainerResponse.class);
    when(responseContext.getEntityStream()).thenReturn(entityStream);
    when(responseContext.getEntityAnnotations()).thenReturn(new Annotation[0]);
    when(responseContext.getMediaType()).thenReturn(MediaType.TEXT_PLAIN_TYPE);
    when(responseContext.getHeaders()).thenReturn(new MultivaluedHashMap<>());

    AtomicReference<Throwable> failure = new AtomicReference<>();
    Thread containerThread = new Thread(() -> {
      try {
        Method setContext = ChunkedOutput.class.getDeclaredMethod("setContext", RequestScope.class, RequestContext.class,
            ContainerRequest.class, ContainerResponse.class, ConnectionCallback.class);
        setContext.setAccessible(true);
        setContext.invoke(writer.m_output, requestScope, mock(RequestContext.class), requestContext, responseContext, mock(ConnectionCallback.class));
      }
      catch (Throwable t) {
        failure.compareAndSet(null, t);
      }
    });
    containerThread.start();
    assertTrue("Container flush did not start", flushStarted.await(5, TimeUnit.SECONDS));

    CountDownLatch closeStarted = new CountDownLatch(1);
    Thread closeThread = new Thread(() -> {
      closeStarted.countDown();
      try {
        writer.close();
      }
      catch (Throwable t) {
        failure.compareAndSet(null, t);
      }
    });
    closeThread.start();
    assertTrue(closeStarted.await(5, TimeUnit.SECONDS));
    awaitBlocked(closeThread);
    assertTrue("close should wait for the container flush", closeThread.isAlive());

    continueFlush.countDown();
    containerThread.join(TimeUnit.SECONDS.toMillis(5));
    closeThread.join(TimeUnit.SECONDS.toMillis(5));

    assertFalse("Container flush did not complete", containerThread.isAlive());
    assertFalse("close did not complete", closeThread.isAlive());
    assertNull(failure.get());
    assertEquals("a\r\nb\r\nc\r\n", received.toString(StandardCharsets.UTF_8));
    assertTrue(writer.isClosed());
    verify(responseContext, atLeastOnce()).close();
  }

  private static void await(CountDownLatch latch) {
    try {
      if (!latch.await(5, TimeUnit.SECONDS)) {
        throw new AssertionError("Timed out waiting for flush release");
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError("Interrupted while waiting for flush release", e);
    }
  }

  private static void awaitBlocked(Thread thread) throws InterruptedException {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while (thread.getState() != Thread.State.BLOCKED && thread.isAlive() && System.nanoTime() < deadline) {
      Thread.yield();
    }
    assertEquals("close did not block on the in-progress flush", Thread.State.BLOCKED, thread.getState());
  }

  protected FixtureIterator fetchData() {
    m_iterator = new FixtureIterator();
    return m_iterator;
  }

  protected static class FixtureIterator implements Iterator<String> {
    final ITransaction m_creatingTxn;
    final Iterator<String> m_delegate;
    final CountDownLatch m_completedLatch;
    private volatile boolean m_iteratesInCreatingTxn = true;

    public FixtureIterator() {
      m_delegate = CollectionUtility.arrayList("a", "b", "c").iterator();
      m_creatingTxn = ITransaction.CURRENT.get();
      m_completedLatch = new CountDownLatch(1);
    }

    @Override
    public boolean hasNext() {
      checkTxn();
      boolean hasNext = m_delegate.hasNext();
      if (!hasNext) {
        m_completedLatch.countDown();
      }
      return hasNext;
    }

    @Override
    public String next() {
      checkTxn();
      return m_delegate.next();
    }

    private void checkTxn() {
      if (m_creatingTxn != ITransaction.CURRENT.get()) {
        m_iteratesInCreatingTxn = false;
      }
    }

    public boolean waitForCompletionAndVerifyTxn() {
      try {
        return m_completedLatch.await(5, TimeUnit.SECONDS) && m_iteratesInCreatingTxn;
      }
      catch (InterruptedException e) {
        throw new ThreadInterruptedError("Interrupted", e);
      }
    }
  }

  @Replace
  @IgnoreBean
  private static class RequiresNewFixtureRunContextFactory extends RunContextFactory {

    @Override
    public RunContext empty() {
      return super.empty()
          .withTransactionScope(TransactionScope.REQUIRES_NEW);
    }

    @Override
    public RunContext copyCurrent() {
      return super.copyCurrent()
          .withTransactionScope(TransactionScope.REQUIRES_NEW);
    }
  }
}
