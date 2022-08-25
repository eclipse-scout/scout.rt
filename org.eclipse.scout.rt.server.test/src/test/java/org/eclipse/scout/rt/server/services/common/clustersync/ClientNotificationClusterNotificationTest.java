/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.services.common.clustersync;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.mom.api.ClusterMom;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.mom.api.NullMomImplementor;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationClusterNotification;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationProperties;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("default")
public class ClientNotificationClusterNotificationTest {
  private static final NodeId TEST_NODE = NodeId.of("node");
  private static final String TEST_USER = "user";

  private IMomImplementor m_nullMomImplementorSpy;
  private List<IBean<?>> m_beans = new ArrayList<>();

  private ClusterSynchronizationService m_svc = null;
  private ClusterNotificationProperties m_testProps = new ClusterNotificationProperties(TEST_NODE, TEST_USER);

  @Before
  public void before() {
    m_nullMomImplementorSpy = spy(NullMomImplementor.class);
    m_beans.add(BeanTestingHelper.get().registerBean(new BeanMetaData(TestClusterMom.class)));
    m_beans.add(BeanTestingHelper.get().registerBean(new BeanMetaData(NullMomImplementor.class).withProducer((IBeanInstanceProducer<IMomImplementor>) bean -> m_nullMomImplementorSpy)));
    // verify that replacement works
    assertSame("NullMomImplementor-Spy expected", m_nullMomImplementorSpy, BEANS.get(NullMomImplementor.class));

    m_svc = new ClusterSynchronizationService();
    m_svc.enable();
    ClientNotificationTestRegistry reg = new ClientNotificationTestRegistry();
    m_beans.add(BeanTestingHelper.get().registerBean(new BeanMetaData(ClientNotificationRegistry.class, reg)));
    reg.registerNode(TEST_NODE);
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
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
    ArrayList<ClientNotificationMessage> messages = new ArrayList<>();
    messages.add(message);
    when(momMsg.getTransferObject()).thenReturn(new ClusterNotificationMessage(new ClientNotificationClusterNotification(messages), m_testProps));

    m_svc.onMessage(momMsg);
    IClientNotificationService c = BEANS.get(IClientNotificationService.class);
    List<ClientNotificationMessage> notifications = c.getNotifications(TEST_NODE);
    assertEquals(1, notifications.size());
  }

  public class ClientNotificationTestRegistry extends ClientNotificationRegistry {
    @Override
    public void registerNode(NodeId nodeId) {
      super.registerNode(nodeId);
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
