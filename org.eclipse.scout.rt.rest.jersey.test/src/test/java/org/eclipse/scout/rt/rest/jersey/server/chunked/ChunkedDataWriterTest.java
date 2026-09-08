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

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.rest.chunked.IChunkedDataWriter;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
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
    exceptionHandler.ignoreExceptionOnce(RuntimeException.class, () -> assertThrows(RuntimeException.class,
        () -> writer.toResponse(() -> {
          throw new RuntimeException("by intention");
        })));

    exceptionHandler.ignoreExceptionOnce(TransactionCancelledError.class, () -> assertThrows(TransactionCancelledError.class,
        () -> writer.toResponse(() -> {
          throw new TransactionCancelledError("by intention");
        })));
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
