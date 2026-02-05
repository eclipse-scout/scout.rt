/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mail.smtp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.SocketException;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;

import org.eclipse.angus.mail.smtp.SMTPSendFailedException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.BooleanHolder;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.testing.platform.mock.RegisterBeanTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockMakers;

public class SmtpConnectionPoolTest {

  protected SmtpHelper m_mockSmtpHelper;
  private Transport m_mockTransport;

  @Rule
  public final RegisterBeanTestRule<SmtpHelper> m_smtpHelperBeanTestRule = new RegisterBeanTestRule<SmtpHelper>(SmtpHelper.class, () -> m_mockSmtpHelper = createMockSmtpHelper());

  protected SmtpHelper createMockSmtpHelper() {
    SmtpHelper mock = mock(SmtpHelper.class);
    Session session = mock(Session.class, withSettings().mockMaker(MockMakers.INLINE));
    try {
      Transport transport = mock(Transport.class);
      when(session.getTransport()).thenReturn(transport);
      m_mockTransport = transport;
    }
    catch (NoSuchProviderException e) {
      throw new ProcessingException("Mocking error", e);
    }
    when(mock.createSession(any())).thenReturn(session);
    return mock;
  }

  @Test
  public void testSendMessage() throws MessagingException {
    SmtpServerConfig config = createDefaultServerConfig();
    SmtpConnectionPool pool = createDefaultSmtpConnectionPool();

    // successful message
    pool.sendMessage(config, mock(MimeMessage.class), new Address[]{mock(Address.class)});

    // failed message
    doThrow(MessagingException.class).when(m_mockTransport).sendMessage(any(), any());
    assertThrows(MessagingException.class, () -> pool.sendMessage(config, mock(MimeMessage.class), new Address[]{mock(Address.class)}));

    // failed message / connection error
    BooleanHolder firstFailed = new BooleanHolder(false);
    BooleanHolder messageSent = new BooleanHolder(false);
    doAnswer(invocation -> {
      if (firstFailed.getValue()) {
        messageSent.setValue(true);
        return null;
      }
      else {
        firstFailed.setValue(true);
        throw new MessagingException("Socket error", new SocketException());
      }
    }).when(m_mockTransport).sendMessage(any(), any());
    pool.sendMessage(config, mock(MimeMessage.class), new Address[]{mock(Address.class)});
    assertTrue(messageSent.getValue());
  }

  @Test
  public void testIsConnectionFailure() {
    SmtpConnectionPool pool = createDefaultSmtpConnectionPool();

    assertFalse(pool.isConnectionFailure(null));

    assertFalse(pool.isConnectionFailure(new MessagingException()));
    assertFalse(pool.isConnectionFailure(new MessagingException("Foo")));
    assertFalse(pool.isConnectionFailure(new MessagingException("", new NullPointerException())));
    assertTrue(pool.isConnectionFailure(new MessagingException("", new SocketException())));

    assertFalse(pool.isConnectionFailure(new SMTPSendFailedException(null, -1, "Foo", null, null, null, null)));
    assertTrue(pool.isConnectionFailure(new SMTPSendFailedException(null, -1, "[EOF]", null, null, null, null)));
    assertFalse(pool.isConnectionFailure(new SMTPSendFailedException(null, 400, "Foo", null, null, null, null)));
    assertTrue(pool.isConnectionFailure(new SMTPSendFailedException(null, 421, "Foo", null, null, null, null)));
    assertTrue(pool.isConnectionFailure(new SMTPSendFailedException(null, 451, "Foo", null, null, null, null)));
  }

  @Test(timeout = 15000)
  public void testLeaseAndReleaseConnection() {
    SmtpServerConfig config = createDefaultServerConfig();
    SmtpConnectionPool pool = createDefaultSmtpConnectionPool();

    assertPoolCount(pool, 0, 0, 0);

    SmtpConnectionPoolEntry entry = pool.leaseConnection(config);
    assertPoolCount(pool, 1, 0, 0);
    assertNotNull(entry);

    pool.releaseConnection(entry);
    assertPoolCount(pool, 0, 1, 0);
  }

  @Test(timeout = 15000)
  public void testLeaseMultipleConnections() {
    SmtpServerConfig config = createDefaultServerConfig();

    SmtpConnectionPool pool = createDefaultSmtpConnectionPool();
    assertPoolCount(pool, 0, 0, 0);

    SmtpConnectionPoolEntry entry = pool.leaseConnection(config);
    assertPoolCount(pool, 1, 0, 0);
    assertNotNull(entry);

    SmtpConnectionPoolEntry entry2 = pool.leaseConnection(config);
    assertPoolCount(pool, 2, 0, 0);
    assertNotNull(entry2);

    pool.releaseConnection(entry);
    assertPoolCount(pool, 1, 1, 0);

    pool.releaseConnection(entry2);
    assertPoolCount(pool, 0, 2, 0);
  }

  /**
   * Test whether multiple connections are requested in parallel and if configured pool size is respected.
   */
  @Test(timeout = 15000)
  public void testLeaseMultipleConnectionsBlockingCreation() {
    SmtpServerConfig config = createDefaultServerConfig();

    SmtpConnectionPool pool = createDefaultSmtpConnectionPool();
    assertPoolCount(pool, 0, 0, 0); // expect an empty pool

    // request first connection (expect pool to count the connection request)
    Pair<IFuture<SmtpConnectionPoolEntry>, IBlockingCondition> request1 = leaseConnectionAsynchronouslyAndBlockUntilConnectionRequested(pool, config);
    assertPoolCount(pool, 0, 0, 1);

    // request second connection (expect pool to count the connection request)
    Pair<IFuture<SmtpConnectionPoolEntry>, IBlockingCondition> request2 = leaseConnectionAsynchronouslyAndBlockUntilConnectionRequested(pool, config);
    assertPoolCount(pool, 0, 0, 2);

    // request again (expect pool to open no other connection request as pool size is limited to 2)
    IFuture<SmtpConnectionPoolEntry> request3 = Jobs.getJobManager().schedule(() -> {
      SmtpConnectionPoolEntry entry = pool.leaseConnection(config);
      assertNotNull(entry);
      return entry;
    }, Jobs.newInput());
    assertPoolCount(pool, 0, 0, 2);

    // fulfill second connection lease request, expect one connection to be leased
    request2.getRight().setBlocking(false);
    SmtpConnectionPoolEntry entry = request2.getLeft().awaitDoneAndGet();
    assertPoolCount(pool, 1, 0, 1);

    // release second entry and expect it to be returned by the third request
    pool.releaseConnection(entry);
    entry = request3.awaitDoneAndGet();
    assertPoolCount(pool, 1, 0, 1);

    // release third entry, expect one idle connection now in pool as there are no additional requests
    pool.releaseConnection(entry);
    assertPoolCount(pool, 0, 1, 1);

    // fulfill first connection lease request, expect connecting count to be zero again, one leased and one idle
    request1.getRight().setBlocking(false);
    entry = request1.getLeft().awaitDoneAndGet();
    assertPoolCount(pool, 1, 1, 0);

    // release first entry, expect two idle entries now
    pool.releaseConnection(entry);
    assertPoolCount(pool, 0, 2, 0);
  }

  @Test(timeout = 15000)
  public void testDestroyAndLease() {
    SmtpServerConfig config = createDefaultServerConfig();
    SmtpConnectionPool pool = createDefaultSmtpConnectionPool();

    pool.destroy();
    assertThrows(AssertionException.class, () -> pool.leaseConnection(config));
  }

  @Test(timeout = 15000)
  public void testDestroyAndRelease() {
    SmtpServerConfig config = createDefaultServerConfig();
    SmtpConnectionPool pool = createDefaultSmtpConnectionPool();
    SmtpConnectionPoolEntry entry = pool.leaseConnection(config);

    pool.destroy();
    assertThrows(AssertionException.class, () -> pool.releaseConnection(entry));
  }

  @Test(timeout = 15000)
  public void testDestroyDuringConnectionCreation() {
    SmtpConnectionPool pool = createDefaultSmtpConnectionPool();

    Pair<IFuture<SmtpConnectionPoolEntry>, IBlockingCondition> request = leaseConnectionAsynchronouslyAndBlockUntilConnectionRequested(pool, createDefaultServerConfig(1));

    pool.destroy();

    request.getRight().setBlocking(false);
    assertThrows(AssertionException.class, request.getLeft()::awaitDoneAndGet);
  }

  /**
   * Lease a connection asynchronously and block until a connection is requested (actually
   * {@link SmtpHelper#createSession(SmtpServerConfig)} is called).
   */
  protected Pair<IFuture<SmtpConnectionPoolEntry>, IBlockingCondition> leaseConnectionAsynchronouslyAndBlockUntilConnectionRequested(SmtpConnectionPool pool, SmtpServerConfig config) {
    IBlockingCondition connectionRequestedBlockingCondition = Jobs.newBlockingCondition(true);
    IBlockingCondition connectionRequestWaitBlockingCondition = Jobs.newBlockingCondition(true);

    doAnswer((inv) -> {
      connectionRequestedBlockingCondition.setBlocking(false);
      connectionRequestWaitBlockingCondition.waitFor();
      return mock(Session.class, withSettings().mockMaker(MockMakers.INLINE));
    }).when(m_mockSmtpHelper).createSession(any());

    IFuture<SmtpConnectionPoolEntry> future = Jobs.getJobManager().schedule(() -> {
      SmtpConnectionPoolEntry entry = pool.leaseConnection(config);
      assertNotNull(entry);
      return entry;
    }, Jobs.newInput());
    connectionRequestedBlockingCondition.waitFor();

    return new ImmutablePair<>(future, connectionRequestWaitBlockingCondition);
  }

  protected void assertPoolCount(SmtpConnectionPool pool, int leaseCount, int idleCount, int connectingCount) {
    pool.runWithPoolLock(() -> {
      assertEquals(leaseCount, pool.m_leasedEntries.size());
      assertEquals(idleCount, pool.m_idleEntries.size());
      int currentlyConnecting = pool.m_currentlyConnectingCounter.values().stream().mapToInt(Integer::intValue).sum();
      assertEquals(connectingCount, currentlyConnecting);
      if (currentlyConnecting == 0) {
        assertTrue(pool.m_currentlyConnectingCounter.isEmpty());
      }
    });
  }

  protected SmtpServerConfig createDefaultServerConfig() {
    int poolSize = 2;
    return createDefaultServerConfig(poolSize);
  }

  protected SmtpServerConfig createDefaultServerConfig(int poolSize) {
    return BEANS.get(SmtpServerConfig.class)
        .withPoolSize(poolSize);
  }

  protected SmtpConnectionPool createDefaultSmtpConnectionPool() {
    SmtpConnectionPool pool = new SmtpConnectionPool() {
      @Override
      protected void startCloseIdleConnectionsJob() {
        // nop: do nothing during tests
      }
    };
    pool.init();
    return pool;
  }
}
