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

import org.eclipse.scout.rt.server.services.common.clientnotification.internal.ClientNotificationQueueElement;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotification;
import org.eclipse.scout.rt.server.services.common.security.AbstractClusterNotificationCoalesceTest;
import org.eclipse.scout.rt.server.services.common.security.AccessControlCacheChangedClusterNotification;
import org.eclipse.scout.rt.shared.services.common.security.ResetAccessControlChangedNotification;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests the coalesce functionality of {@link ClientNotificationClusterNotification}
 */
public class ClientNotificationClusterNotificationCoalesceTest extends AbstractClusterNotificationCoalesceTest<ClientNotificationClusterNotification> {

  private IClientNotificationQueueElement m_queueElement1;
  private IClientNotificationQueueElement m_queueElement2;

  @Before
  @Override
  public void before() {
    m_queueElement1 = new ClientNotificationQueueElement(new ResetAccessControlChangedNotification(), new SingleUserFilter(null, 0L));
    m_queueElement2 = new ClientNotificationQueueElement(new ResetAccessControlChangedNotification(), new SingleUserFilter(null, 0L));
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
  protected IClusterNotification createDifferentNotification() {
    return new AccessControlCacheChangedClusterNotification();
  }

  @Override
  protected boolean isCoalesceExpected() {
    return true;
  }

  @Override
  protected void checkCoalesceResultSuccess(ClientNotificationClusterNotification notificationToCheck) {
    checkNotification(notificationToCheck);
  }

  protected void checkNotification(ClientNotificationClusterNotification notificationToCheck) {
    Assert.assertTrue(notificationToCheck.getQueueElement().getNotification() instanceof ResetAccessControlChangedNotification);
    Assert.assertTrue(notificationToCheck.getQueueElement().getFilter() instanceof SingleUserFilter);
    Assert.assertNull(((SingleUserFilter) notificationToCheck.getQueueElement().getFilter()).getUserId());
  }

  @Override
  protected void checkCoalesceResultFail(ClientNotificationClusterNotification notificationToCheck) {
    checkNotification(notificationToCheck);
  }

}
