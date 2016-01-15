package org.eclipse.scout.rt.server.services.common.clustersync;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationClusterNotification;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationProperties;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h3>{@link ClientNotificationClusterNotificationTest}</h3>
 *
 * @author jgu
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("default")
public class ClientNotificationClusterNotificationTest {
  private static final String TEST_NODE = "node";
  private static final String TEST_USER = "user";
  private IBean<?> m_registration;

  @BeanMock
  private IPublishSubscribeMessageService m_messageService;
  private ClusterSynchronizationService m_svc = null;
  private ClusterNotificationProperties m_testProps = new ClusterNotificationProperties(TEST_NODE, TEST_USER);

  @Before
  public void before() {
    m_svc = new ClusterSynchronizationService();
    m_svc.initializeService();
    m_svc.enable();
    ClientNotificationTestRegistry reg = new ClientNotificationTestRegistry();
    m_registration = TestingUtility.registerBean(new BeanMetaData(ClientNotificationRegistry.class, reg));
    reg.registerSession(TEST_NODE, "test", TEST_USER);
  }

  @After
  public void after() {
    TestingUtility.unregisterBean(m_registration);
  }

  /**
   * Tests that the client notifications are added to the queue when received
   */
  @Test
  public void testClientNotificationsReceived() {
    ClientNotificationAddress address = ClientNotificationAddress.createAllNodesAddress();
    ClientNotificationMessage message = new ClientNotificationMessage(address, "test", true);
    ArrayList<ClientNotificationMessage> messages = new ArrayList<ClientNotificationMessage>();
    messages.add(message);
    ClientNotificationClusterNotification n = new ClientNotificationClusterNotification(messages);
    ClusterNotificationMessage m = new ClusterNotificationMessage(n, m_testProps);
    m_svc.onMessage(m);
    IClientNotificationService c = BEANS.get(IClientNotificationService.class);
    List<ClientNotificationMessage> notifications = c.getNotifications(TEST_NODE);
    assertEquals(notifications.size(), 1);
  }

  public class ClientNotificationTestRegistry extends ClientNotificationRegistry {
    @Override
    public void registerSession(String nodeId, String sessionId, String userId) {
      super.registerSession(nodeId, sessionId, userId);
    }
  }
}
