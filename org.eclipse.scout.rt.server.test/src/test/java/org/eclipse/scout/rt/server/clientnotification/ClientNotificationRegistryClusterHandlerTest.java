/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.clientnotification;

import static org.mockito.Mockito.*;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistryClusterNotification.Event;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testcases for {@link ClientNotificationRegistryClusterHandler}
 */
@RunWith(PlatformTestRunner.class)
public class ClientNotificationRegistryClusterHandlerTest {

  protected static final NodeId MOCK_CLIENT_ID = NodeId.of("mockClientId");

  @BeanMock
  protected ClientNotificationRegistry m_registry;

  @Test
  public void testRegister() {
    ClientNotificationRegistryClusterHandler handler = BEANS.get(ClientNotificationRegistryClusterHandler.class);
    handler.handleNotification(new ClientNotificationRegistryClusterNotification(Event.NODE_REGISTERED, MOCK_CLIENT_ID));
    verify(m_registry).registerNode(MOCK_CLIENT_ID, false);
    verifyNoMoreInteractions(m_registry);
  }

  @Test
  public void testUnregister() {
    ClientNotificationRegistryClusterHandler handler = BEANS.get(ClientNotificationRegistryClusterHandler.class);
    handler.handleNotification(new ClientNotificationRegistryClusterNotification(Event.NODE_UNREGISTERED, MOCK_CLIENT_ID));
    verify(m_registry).unregisterNode(MOCK_CLIENT_ID, false);
    verifyNoMoreInteractions(m_registry);
  }
}
