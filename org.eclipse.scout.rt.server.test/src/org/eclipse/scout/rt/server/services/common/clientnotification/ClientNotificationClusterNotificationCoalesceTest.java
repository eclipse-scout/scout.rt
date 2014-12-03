/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.clientnotification;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.server.services.common.clientnotification.internal.ClientNotificationQueueElement;
import org.eclipse.scout.rt.server.services.common.clustersync.AbstractClusterNotificationCoalesceTest;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotification;
import org.eclipse.scout.rt.server.services.common.security.AccessControlCacheChangedClusterNotification;
import org.eclipse.scout.rt.shared.services.common.security.AccessControlChangedNotification;
import org.eclipse.scout.rt.shared.services.common.security.ResetAccessControlChangedNotification;
import org.junit.Before;

/**
 * Tests the coalesce functionality of {@link ClientNotificationClusterNotification}
 */
public class ClientNotificationClusterNotificationCoalesceTest extends AbstractClusterNotificationCoalesceTest<ClientNotificationClusterNotification> {

  private IClientNotificationQueueElement m_queueElement1;
  private IClientNotificationQueueElement m_queueElement2;
  private IClientNotificationQueueElement m_queueElement3;

  @Before
  @Override
  public void before() {
    m_queueElement1 = new ClientNotificationQueueElement(new ResetAccessControlChangedNotification(), new SingleUserFilter(null, 0L));
    m_queueElement2 = new ClientNotificationQueueElement(new ResetAccessControlChangedNotification(), new SingleUserFilter(null, 0L));
    m_queueElement3 = new ClientNotificationQueueElement(new AccessControlChangedNotification(null), new SingleUserFilter(null, 0L));
    super.before();
  }

  @Override
  protected ClientNotificationClusterNotification createExistingNotification() {
    return new ClientNotificationClusterNotification(m_queueElement1);
  }

  @Override
  protected ClientNotificationClusterNotification createNewNotification() {
    return new ClientNotificationClusterNotification(m_queueElement2);
  }

  @Override
  protected ClientNotificationClusterNotification createNewNonMergeableNotification() {
    return new ClientNotificationClusterNotification(m_queueElement3);
  }

  @Override
  protected IClusterNotification createDifferentNotification() {
    return new AccessControlCacheChangedClusterNotification();
  }

  @Override
  protected boolean isCoalesceExpected() {
    return true;
  }

  @Override
  protected void checkCoalesceResult(ClientNotificationClusterNotification notificationToCheck) {
    checkNotification(notificationToCheck);
  }

  @Override
  protected void checkCoalesceFailResult(ClientNotificationClusterNotification notificationToCheck) {
    checkNotification(notificationToCheck);
  }

  @Override
  protected void checkCoalesceDifferentNotificationResult(ClientNotificationClusterNotification notificationToCheck) {
    checkNotification(notificationToCheck);
  }

  protected void checkNotification(ClientNotificationClusterNotification notificationToCheck) {
    assertTrue(notificationToCheck.getQueueElement().getNotification() instanceof ResetAccessControlChangedNotification);
    assertTrue(notificationToCheck.getQueueElement().getFilter() instanceof SingleUserFilter);
    assertNull(((SingleUserFilter) notificationToCheck.getQueueElement().getFilter()).getUserId());
  }
}
