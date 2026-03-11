/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.clustersync;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.mom.api.ClusterMom;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.mom.api.NullMomImplementor;
import org.eclipse.scout.rt.mom.api.PublishInput;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.cache.AllCacheEntryFilter;
import org.eclipse.scout.rt.platform.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.platform.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.server.mom.IClusterMomDestinations;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationProperties;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkChangedClientNotification;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

/**
 * Tests for {@link ClusterSynchronizationService} without transactions.
 */
@RunWith(ServerTestRunner.class)
@RunWithSubject("default")
public class ClusterSynchronizationServiceTest {
  private static final NodeId TEST_NODE = NodeId.of("node");
  private static final String TEST_USER = "user";
  private static final String TEST_CACHE_ID = "cacheId";

  private ClusterNotificationMessage m_message;
  private ClusterNotificationMessage m_cacheInvalidationMessage;
  private ClusterSynchronizationService m_svc = null;

  private IMomImplementor m_nullMomImplementorSpy;
  private List<IBean<?>> m_beans = new ArrayList<>();

  @Before
  public void before() {
    m_nullMomImplementorSpy = spy(NullMomImplementor.class);
    m_beans.add(BeanTestingHelper.get().registerBean(new BeanMetaData(TestClusterMom.class)));
    m_beans.add(BeanTestingHelper.get().registerBean(new BeanMetaData(NullMomImplementor.class).withProducer((IBeanInstanceProducer<IMomImplementor>) bean -> m_nullMomImplementorSpy)));
    // verify that replacement works
    assertSame("NullMomImplementor-Spy expected", m_nullMomImplementorSpy, BEANS.get(NullMomImplementor.class));

    ClusterNotificationProperties testProps = new ClusterNotificationProperties(TEST_NODE, TEST_USER);
    m_message = new ClusterNotificationMessage("notification", testProps);
    m_cacheInvalidationMessage = new ClusterNotificationMessage(new InvalidateCacheNotification(TEST_CACHE_ID, mock(ICacheEntryFilter.class)), testProps);

    m_svc = new ClusterSynchronizationService();
    m_svc.enable();
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
  }

  /**
   * Tests that the statusInfo is updated up on receipt of a message.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testReceiveInfoUpdated() {
    IMessage<IClusterNotificationMessage> momMsg = mock(IMessage.class);
    when(momMsg.getTransferObject()).thenReturn(m_message);
    m_svc.onMessage(momMsg);

    IClusterNodeStatusInfo statusInfo = m_svc.getStatusInfo();
    assertEquals(1, statusInfo.getReceivedMessageCount());
    assertEquals(0, statusInfo.getSentMessageCount());
    assertEquals(TEST_NODE, statusInfo.getLastChangedOriginNodeId());
    assertEquals(TEST_USER, statusInfo.getLastChangedUserId());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testStatusForMessageType() {
    IMessage<IClusterNotificationMessage> momMsg = mock(IMessage.class);
    when(momMsg.getTransferObject()).thenReturn(m_message);
    m_svc.onMessage(momMsg);

    IClusterNodeStatusInfo status = m_svc.getStatusInfo(String.class);
    assertEquals(1, status.getReceivedMessageCount());
    assertEquals(0, status.getSentMessageCount());
    assertEquals(TEST_NODE, status.getLastChangedOriginNodeId());
    assertEquals(TEST_USER, status.getLastChangedUserId());

    assertEmptyStatusInfo(m_svc.getStatusInfo(Long.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCacheInvalidationStatusWithoutCacheId() {
    IMessage<IClusterNotificationMessage> momMsg = mock(IMessage.class);
    when(momMsg.getTransferObject()).thenReturn(m_cacheInvalidationMessage);
    m_svc.onMessage(momMsg);

    // status for invalidation notification requires a cache id - getCacheInvalidationStatusInfo should be used
    IClusterNodeStatusInfo status = m_svc.getStatusInfo(InvalidateCacheNotification.class);
    assertEmptyStatusInfo(status);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCacheInvalidationStatus() {
    IMessage<IClusterNotificationMessage> momMsg = mock(IMessage.class);
    when(momMsg.getTransferObject()).thenReturn(m_cacheInvalidationMessage);
    m_svc.onMessage(momMsg);

    IClusterNodeStatusInfo status = m_svc.getCacheInvalidationStatusInfo(TEST_CACHE_ID);
    assertEquals(1, status.getReceivedMessageCount());
    assertEquals(0, status.getSentMessageCount());
    assertEquals(TEST_NODE, status.getLastChangedOriginNodeId());
    assertEquals(TEST_USER, status.getLastChangedUserId());

    assertEmptyStatusInfo(m_svc.getCacheInvalidationStatusInfo("notExistingCacheId"));
  }

  /**
   * Tests that the message is sent when publishing it.
   */
  @Test
  public void testSendNoTransaction() {
    m_svc.publish("Testnotification");
    assertSingleMessageSent();
  }

  /**
   * Tests that no message is sent, if the transaction is not committed.
   */
  @Test
  public void testSendTransactional_NotCommitted() {
    m_svc.publishTransactional("Testnotification");
    assertNoMessageSent();
  }

  /**
   * Tests that no message is sent, if the transaction is not committed.
   */
  @Test
  public void testSendTransactional_Committed() {
    m_svc.publishTransactional("Testnotification");
    ITransaction.CURRENT.get().commitPhase1();
    ITransaction.CURRENT.get().commitPhase2();
    assertSingleMessageSent();
  }

  @Test
  public void testSendTransactional_Rollback() {
    m_svc.publishTransactional("Testnotification");
    ITransaction.CURRENT.get().rollback();
    ITransaction.CURRENT.get().commitPhase1();
    ITransaction.CURRENT.get().commitPhase2();
    assertNoMessageSent();
  }

  @Test
  public void testTransactionalSendMultipleMessages() {
    ArgumentCaptor<ClusterNotificationMessage> msgCaptor = ArgumentCaptor.forClass(ClusterNotificationMessage.class);
    doNothing().when(m_nullMomImplementorSpy).publish(eq(IClusterMomDestinations.CLUSTER_NOTIFICATION_TOPIC), msgCaptor.capture(), any(PublishInput.class));

    m_svc.publishTransactional("Testnotification1");
    m_svc.publishTransactional("Testnotification2");
    ITransaction.CURRENT.get().commitPhase1();
    ITransaction.CURRENT.get().commitPhase2();

    // verify
    verify(m_nullMomImplementorSpy, times(2)).publish(eq(IClusterMomDestinations.CLUSTER_NOTIFICATION_TOPIC), any(IClusterNotificationMessage.class), any(PublishInput.class));
    assertEquals(2, m_svc.getStatusInfo().getSentMessageCount());

    List<ClusterNotificationMessage> messages = msgCaptor.getAllValues();
    assertEquals("Testnotification1", messages.get(0).getNotification());
    assertEquals("Testnotification2", messages.get(1).getNotification());
  }

  @Test
  public void testDisabledSendTransactional() {
    m_svc.disable();
    m_svc.publishTransactional("Testnotification");
    ITransaction.CURRENT.get().commitPhase1();
    ITransaction.CURRENT.get().commitPhase2();
    assertNoMessageSent();
  }

  @Test
  public void testDisabledSend_NoTransaction() {
    m_svc.disable();
    m_svc.publish("Testnotification");
    assertNoMessageSent();
  }

  @Test
  public void testTransactionalWithCoalesce() {
    ArgumentCaptor<ClusterNotificationMessage> msgCaptor = ArgumentCaptor.forClass(ClusterNotificationMessage.class);
    doNothing().when(m_nullMomImplementorSpy).publish(eq(IClusterMomDestinations.CLUSTER_NOTIFICATION_TOPIC), msgCaptor.capture(), any(PublishInput.class));

    m_svc.publishTransactional(new BookmarkChangedClientNotification());
    m_svc.publishTransactional(new BookmarkChangedClientNotification());
    m_svc.publishTransactional(new InvalidateCacheNotification("TEST", new AllCacheEntryFilter<>()));
    ITransaction.CURRENT.get().commitPhase1();
    ITransaction.CURRENT.get().commitPhase2();

    // verify
    verify(m_nullMomImplementorSpy, times(2)).publish(eq(IClusterMomDestinations.CLUSTER_NOTIFICATION_TOPIC), any(IClusterNotificationMessage.class), any(PublishInput.class));
    assertEquals(2, m_svc.getStatusInfo().getSentMessageCount());

    List<ClusterNotificationMessage> messages = msgCaptor.getAllValues();
    assertEquals(BookmarkChangedClientNotification.class, messages.get(0).getNotification().getClass());
    assertEquals(InvalidateCacheNotification.class, messages.get(1).getNotification().getClass());
  }

  private void assertNoMessageSent() {
    verify(m_nullMomImplementorSpy, never()).publish(eq(IClusterMomDestinations.CLUSTER_NOTIFICATION_TOPIC), any(IClusterNotificationMessage.class), any(PublishInput.class));
    assertEmptyStatusInfo(m_svc.getStatusInfo());
  }

  private void assertSingleMessageSent() {
    verify(m_nullMomImplementorSpy, times(1)).publish(eq(IClusterMomDestinations.CLUSTER_NOTIFICATION_TOPIC), any(IClusterNotificationMessage.class), any(PublishInput.class));
    IClusterNodeStatusInfo statusInfo = m_svc.getStatusInfo();
    assertEquals(0, statusInfo.getReceivedMessageCount());
    assertEquals(1, statusInfo.getSentMessageCount());
    assertEquals(NodeId.current(), statusInfo.getLastChangedOriginNodeId());
    assertEquals("default", statusInfo.getLastChangedUserId());
  }

  private void assertEmptyStatusInfo(IClusterNodeStatusInfo status) {
    assertEquals(0, status.getReceivedMessageCount());
    assertEquals(0, status.getSentMessageCount());
    assertNull(status.getLastChangedOriginNodeId());
    assertNull(status.getLastChangedUserId());
  }

  class TestCodeType extends AbstractCodeType<Long, Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return null;
    }
  }

  @IgnoreBean
  @Replace
  public static class TestClusterMom extends ClusterMom {

    @Override
    protected Class<? extends IMomImplementor> getConfiguredImplementor() {
      return NullMomImplementor.class;
    }

    @Override
    public boolean isNullTransport() {
      // Because we use the NullMomImplementor in the test, the ClusterSynchronizationService could
      // not be enabled. For the test, we intentionally lie here about the type of transport.
      return false;
    }
  }
}
