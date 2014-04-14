package org.eclipse.scout.rt.client.services.common.clientnotification.internal;

import static org.eclipse.scout.rt.testing.commons.ScoutAssert.assertSetEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.clientnotification.ClientNotificationConsumerEvent;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerListener;
import org.eclipse.scout.rt.shared.services.common.clientnotification.AbstractClientNotification;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link ClientNotificationConsumerService}
 */
@RunWith(ScoutClientTestRunner.class)
public class ClientNotificationConsumerServiceTest {
  /** Service under test */
  private ClientNotificationConsumerService m_clientNotificationService;
  /** Listener for current session */
  private IClientNotificationConsumerListener m_sessionListener;
  /** Listener for all sessions */
  private IClientNotificationConsumerListener m_globalListener;
  /** Listener for m_testSession, blocking */
  private BlockingNotificationConsumerListener m_testSessionListener;
  /** Listener for all sessions, blocking */
  private BlockingNotificationConsumerListener m_globalBlockingListener;
  /** Test notification id */
  private static final String TEST_ID = "testId";
  private IClientSession m_testSession;
  private List<IClientNotification> m_testNotifications;

  @Before
  public void setup() {
    m_clientNotificationService = new ClientNotificationConsumerService();
    m_sessionListener = mock(IClientNotificationConsumerListener.class);
    m_clientNotificationService.addClientNotificationConsumerListener(ClientSyncJob.getCurrentSession(), m_sessionListener);
    m_globalListener = mock(IClientNotificationConsumerListener.class);
    m_clientNotificationService.addGlobalClientNotificationConsumerListener(m_globalListener);
    m_testSession = new AbstractClientSession(false) {
    };
    m_testSessionListener = new BlockingNotificationConsumerListener();
    m_clientNotificationService.addClientNotificationConsumerListener(m_testSession, m_testSessionListener);
    m_globalBlockingListener = new BlockingNotificationConsumerListener();
    m_clientNotificationService.addGlobalClientNotificationConsumerListener(m_globalBlockingListener);

    final int timeout = 1000 * 60;
    m_testNotifications = CollectionUtility.arrayList(createTestNotification(timeout));
  }

  @After
  public void tearDown() {
    m_clientNotificationService.removeGlobalClientNotificationConsumerListener(m_globalListener);
    m_clientNotificationService.removeClientNotificationConsumerListener(ClientSyncJob.getCurrentSession(), m_sessionListener);
    m_clientNotificationService.removeClientNotificationConsumerListener(m_testSession, m_testSessionListener);
    m_clientNotificationService.removeConsumedNotificationIds(getTestIdSet(), ClientSyncJob.getCurrentSession());
    m_clientNotificationService.removeConsumedNotificationIds(getTestIdSet(), m_testSession);
    m_clientNotificationService.removeGlobalConsumedNotificationIds(getTestIdSet());
  }

  /**
   * Tests that no notifications are dispatched for an empty notification list.
   */
  @Test
  public void testNoNotificationReceived() {
    m_clientNotificationService.dispatchClientNotifications(new ArrayList<IClientNotification>(), ClientSyncJob.getCurrentSession());
    verifyZeroInteractions(m_sessionListener);
    verifyZeroInteractions(m_globalListener);
  }

  /**
   * Tests that a notification is dispatched.
   */
  @Test
  public void testNotificationReceive() throws InterruptedException {
    dispatchTestNotifications(ClientSyncJob.getCurrentSession());
    verifyNotificationReceived(m_sessionListener);
    verifyNotificationReceived(m_globalListener);
    assertSetEquals(getTestIdSet(), m_clientNotificationService.getConsumedNotificationIds(ClientSyncJob.getCurrentSession()));
  }

  /**
   * Tests that a notification is dispatched for a different session.
   */
  @Test
  public void testNotificationReceivedForSession() throws InterruptedException {
    m_clientNotificationService.dispatchClientNotifications(m_testNotifications, m_testSession);
    assertEquals(TEST_ID, m_testSessionListener.waitForHandleEvent().getId());
    assertEquals(TEST_ID, m_globalBlockingListener.waitForHandleEvent().getId());
    assertSetEquals(getTestIdSet(), m_clientNotificationService.getConsumedNotificationIds(m_testSession));
    assertSetEquals(getTestIdSet(), m_clientNotificationService.getGlobalConsumedNotificationIds());
    verifyZeroInteractions(m_sessionListener);
  }

  /**
   * Tests that client notifications are only consumed once.
   */
  @Test
  public void testNotificationOnlyConsumedOnce() {
    dispatchTestNotifications(ClientSyncJob.getCurrentSession());
    verifyNotificationReceived(m_sessionListener);
    verifyNotificationReceived(m_globalListener);
    assertSetEquals(getTestIdSet(), m_clientNotificationService.getConsumedNotificationIds(ClientSyncJob.getCurrentSession()));
    dispatchTestNotifications(ClientSyncJob.getCurrentSession());
    verifyNoMoreInteractions(m_sessionListener);
    verifyNoMoreInteractions(m_globalListener);
    assertSetEquals(getTestIdSet(), m_clientNotificationService.getConsumedNotificationIds(ClientSyncJob.getCurrentSession()));
  }

  @Test
  public void testExpiredNotificationCleanedUp() {
    final int noncachedTimeout = -30000;
    ArrayList<IClientNotification> notifications = CollectionUtility.arrayList(createTestNotification(noncachedTimeout));
    m_clientNotificationService.dispatchClientNotifications(notifications, ClientSyncJob.getCurrentSession());
    m_clientNotificationService.dispatchClientNotifications(notifications, ClientSyncJob.getCurrentSession());
    verify(m_sessionListener, times(2)).handleEvent(any(ClientNotificationConsumerEvent.class), anyBoolean());
    verify(m_globalListener, times(2)).handleEvent(any(ClientNotificationConsumerEvent.class), anyBoolean());
  }

  /**
   * Tests that client notifications consumed for another session is still consumed.
   * 
   * @throws InterruptedException
   */
  @Test
  public void testNotificationConsumedOnOtherSession() throws InterruptedException {
    dispatchTestNotifications(ClientSyncJob.getCurrentSession());
    m_clientNotificationService.dispatchClientNotifications(m_testNotifications, m_testSession);
    IClientNotification notification = m_testSessionListener.waitForHandleEvent();
    assertEquals(TEST_ID, notification.getId());
  }

  private void dispatchTestNotifications(IClientSession session) {
    m_clientNotificationService.dispatchClientNotifications(m_testNotifications, session);
  }

  private void verifyNotificationReceived(IClientNotificationConsumerListener listener) {
    verify(listener, times(1)).handleEvent(any(ClientNotificationConsumerEvent.class), anyBoolean());
  }

  private Set<String> getIds(Collection<IClientNotification> notifications) {
    HashSet<String> ids = new HashSet<String>();
    for (IClientNotification n : notifications) {
      ids.add(n.getId());
    }
    return ids;
  }

  /**
   * @return notification with {@link #TEST_ID}
   */
  private IClientNotification createTestNotification(long timeout) {
    return new AbstractClientNotification(timeout) {

      private static final long serialVersionUID = 1L;

      @Override
      public boolean coalesce(IClientNotification existingNotification) {
        return false;
      }

      @Override
      public String getId() {
        return TEST_ID;
      }
    };
  }

  private Set<String> getTestIdSet() {
    return getIds(m_testNotifications);
  }

  private static class BlockingNotificationConsumerListener implements IClientNotificationConsumerListener {
    private final CountDownLatch m_latch = new CountDownLatch(1);
    private volatile IClientNotification m_notification;

    /**
     * Blocks until handleEvent is called and returns the received notification
     */
    public IClientNotification waitForHandleEvent() throws InterruptedException {
      m_latch.await(1, TimeUnit.SECONDS);
      return m_notification;
    }

    @Override
    public void handleEvent(ClientNotificationConsumerEvent e, boolean sync) {
      m_notification = e.getClientNotification();
      m_latch.countDown();
    }
  }

}
