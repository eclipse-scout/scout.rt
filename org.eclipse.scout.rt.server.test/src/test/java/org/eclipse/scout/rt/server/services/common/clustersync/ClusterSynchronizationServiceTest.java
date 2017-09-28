/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.clustersync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.scout.rt.platform.context.NodeIdentifier;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.mom.IClusterMomDestinations;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationProperties;
import org.eclipse.scout.rt.shared.cache.AllCacheEntryFilter;
import org.eclipse.scout.rt.shared.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkChangedClientNotification;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

/**
 * Tests for {@link ClusterSynchronizationService} without transactions.
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("default")
public class ClusterSynchronizationServiceTest {
  private static final String TEST_NODE = "node";
  private static final String TEST_USER = "user";

  private ClusterNotificationMessage m_message;
  private ClusterSynchronizationService m_svc = null;

  private IMomImplementor m_nullMomImplementorSpy;
  private List<IBean<?>> m_beans = new ArrayList<>();

  @Before
  public void before() throws Exception {
    m_nullMomImplementorSpy = spy(NullMomImplementor.class);
    m_beans.add(TestingUtility.registerBean(new BeanMetaData(TestClusterMom.class)));
    m_beans.add(TestingUtility.registerBean(new BeanMetaData(NullMomImplementor.class).withProducer(new IBeanInstanceProducer<IMomImplementor>() {

      @Override
      public IMomImplementor produce(IBean<IMomImplementor> bean) {
        return m_nullMomImplementorSpy;
      }
    })));
    // verify that replacement works
    assertSame("NullMomImplementor-Spy expected", m_nullMomImplementorSpy, BEANS.get(NullMomImplementor.class));

    ClusterNotificationProperties testProps = new ClusterNotificationProperties(TEST_NODE, TEST_USER);
    m_message = new ClusterNotificationMessage("notification", testProps);

    m_svc = new ClusterSynchronizationService();
    m_svc.enable();
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_beans);
  }

  /**
   * Tests that the statusInfo is updated up on receipt of a message.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testReveiveInfoUpdated() {
    IMessage<IClusterNotificationMessage> momMsg = mock(IMessage.class);
    when(momMsg.getTransferObject()).thenReturn(m_message);
    m_svc.onMessage(momMsg);

    IClusterNodeStatusInfo nodeInfo = m_svc.getStatusInfo();
    assertEquals(1, nodeInfo.getReceivedMessageCount());
    assertEquals(0, nodeInfo.getSentMessageCount());
    assertEquals(TEST_NODE, nodeInfo.getLastChangedOriginNodeId());
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
  public void testSendTransactional_NotCommitted() throws Exception {
    m_svc.publishTransactional("Testnotification");
    assertNoMessageSent();
  }

  /**
   * Tests that no message is sent, if the transaction is not committed.
   */
  @Test
  public void testSendTransactional_Committed() throws Exception {
    m_svc.publishTransactional("Testnotification");
    ITransaction.CURRENT.get().commitPhase1();
    ITransaction.CURRENT.get().commitPhase2();
    assertSingleMessageSent();
  }

  @Test
  public void testSendTransactional_Rollback() throws Exception {
    m_svc.publishTransactional("Testnotification");
    ITransaction.CURRENT.get().rollback();
    ITransaction.CURRENT.get().commitPhase1();
    ITransaction.CURRENT.get().commitPhase2();
    assertNoMessageSent();
  }

  @Test
  public void testTransactionalSendMultipleMessages() throws Exception {
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
  public void testDisabledSendTransactional() throws Exception {
    m_svc.disable();
    m_svc.publishTransactional("Testnotification");
    ITransaction.CURRENT.get().commitPhase1();
    ITransaction.CURRENT.get().commitPhase2();
    assertNoMessageSent();
  }

  @Test
  public void testDisabledSend_NoTransaction() throws Exception {
    m_svc.disable();
    m_svc.publish("Testnotification");
    assertNoMessageSent();
  }

  @Test
  public void testTransactionalWithCoalesce() throws Exception {
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
    assertEmptyNodeInfo(m_svc.getStatusInfo());
  }

  private void assertSingleMessageSent() {
    verify(m_nullMomImplementorSpy, times(1)).publish(eq(IClusterMomDestinations.CLUSTER_NOTIFICATION_TOPIC), any(IClusterNotificationMessage.class), any(PublishInput.class));
    IClusterNodeStatusInfo statusInfo = m_svc.getStatusInfo();
    assertEquals(0, statusInfo.getReceivedMessageCount());
    assertEquals(1, statusInfo.getSentMessageCount());
    assertEquals(BEANS.get(NodeIdentifier.class).get(), statusInfo.getLastChangedOriginNodeId());
    assertEquals("default", statusInfo.getLastChangedUserId());
  }

  private void assertEmptyNodeInfo(IClusterNodeStatusInfo status) {
    assertEquals(0, status.getReceivedMessageCount());
    assertEquals(0, status.getSentMessageCount());
    assertEquals(null, status.getLastChangedOriginNodeId());
    assertEquals(null, status.getLastChangedUserId());
  }

  class TestCodeType extends AbstractCodeType<Long, Long> {

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
