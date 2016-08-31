package org.eclipse.scout.rt.server.services.common.clustersync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.mom.api.ClusterMom;
import org.eclipse.scout.rt.mom.api.ClusterMom.ClusterMomImplementorProperty;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.mom.api.NullMomImplementor;
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
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h3>{@link ClientNotificationClusterNotificationTest}</h3>
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("default")
public class ClientNotificationClusterNotificationTest {
  private static final String TEST_NODE = "node";
  private static final String TEST_USER = "user";

  private IMom m_nullMomImplementorSpy;
  private List<IBean<?>> m_beans = new ArrayList<>();

  private ClusterSynchronizationService m_svc = null;
  private ClusterNotificationProperties m_testProps = new ClusterNotificationProperties(TEST_NODE, TEST_USER);

  @Before
  public void before() throws Exception {
    m_nullMomImplementorSpy = spy(NullMomImplementor.class);
    m_beans.add(TestingUtility.registerBean(new BeanMetaData(ClusterMom.class))); // Register ClusterMom anew to force initialization (application-scoped)
    m_beans.add(TestingUtility.registerBean(new BeanMetaData(NullMomImplementorProperty.class).withReplace(true))); // Ensure to use NullMom
    m_beans.add(TestingUtility.registerBean(new BeanMetaData(NullMomImplementor.class).withInitialInstance(m_nullMomImplementorSpy)));
    // verify that replacement works
    assertSame("NullMom-Spy expected", m_nullMomImplementorSpy, BEANS.get(NullMomImplementor.class));

    m_svc = new ClusterSynchronizationService();
    m_svc.enable();
    ClientNotificationTestRegistry reg = new ClientNotificationTestRegistry();
    m_beans.add(TestingUtility.registerBean(new BeanMetaData(ClientNotificationRegistry.class, reg)));
    reg.registerSession(TEST_NODE, "test", TEST_USER);
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_beans);
  }

  /**
   * Tests that the client notifications are added to the queue when received
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testClientNotificationsReceived() {
    IMessage<IClusterNotificationMessage> momMsg = mock(IMessage.class);

    ClientNotificationAddress address = ClientNotificationAddress.createAllNodesAddress();
    ClientNotificationMessage message = new ClientNotificationMessage(address, "test", true, "cid");
    ArrayList<ClientNotificationMessage> messages = new ArrayList<ClientNotificationMessage>();
    messages.add(message);
    when(momMsg.getTransferObject()).thenReturn(new ClusterNotificationMessage(new ClientNotificationClusterNotification(messages), m_testProps));

    m_svc.onMessage(momMsg);
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

  @Ignore
  private static class NullMomImplementorProperty extends ClusterMomImplementorProperty {

    @Override
    protected Class<? extends IMomImplementor> createValue() {
      return NullMomImplementor.class;
    }
  }
}
