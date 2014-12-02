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
package org.eclipse.scout.rt.server.services.common.security;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.server.services.common.clientnotification.ClientNotificationClusterNotification;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotification;
import org.junit.BeforeClass;

/**
 * Tests the coalesce functionality of {@link AccessControlCacheChangedClusterNotification}
 */
public class AccessControlCacheChangedClusterNotificationCoalesceTest extends AbstractClusterNotificationCoalesceTest<AccessControlCacheChangedClusterNotification> {

  private static Set<String> m_userIds1;
  private static Set<String> m_userIds2;

  @BeforeClass
  public static void beforeClass() {
    m_userIds1 = new HashSet<String>();
    m_userIds1.add("User1");
    m_userIds1.add("User2");

    m_userIds2 = new HashSet<String>();
    m_userIds2.add("Person1");
    m_userIds2.add("Person2");
  }

  @Override
  protected AccessControlCacheChangedClusterNotification createExistingNotification() {
    return new AccessControlCacheChangedClusterNotification(new HashSet<String>(m_userIds1));
  }

  @Override
  protected AccessControlCacheChangedClusterNotification createNewNotification() {
    return new AccessControlCacheChangedClusterNotification(new HashSet<String>(m_userIds2));
  }

  @Override
  protected IClusterNotification createDifferentNotification() {
    return new ClientNotificationClusterNotification(null);
  }

  @Override
  protected boolean isCoalesceExpected() {
    return true;
  }

  @Override
  protected void checkCoalesceResultSuccess(AccessControlCacheChangedClusterNotification notificationToCheck) {
    Set<String> userIds = new HashSet<String>(m_userIds1);
    userIds.addAll(m_userIds2);
    assertEquals(userIds, notificationToCheck.getUserIds());
  }

  @Override
  protected void checkCoalesceResultFail(AccessControlCacheChangedClusterNotification notificationToCheck) {
    Set<String> expectedList = new HashSet<String>(m_userIds1);
    assertEquals(expectedList, notificationToCheck.getUserIds());
  }
}
