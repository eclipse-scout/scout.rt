/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.services.common.security.AccessControlClusterNotification;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link NotificationCoalescer}
 */
@RunWith(PlatformTestRunner.class)
public class NotificationCoalescerTest {

  @Test
  public void testCoalesceEmptySet() {
    List<? extends Serializable> res = BEANS.get(NotificationCoalescer.class).coalesce(new ArrayList<Serializable>());
    assertTrue(res.isEmpty());
  }

  @Test
  public void testAccessControlCoalesce_single() throws Exception {
    List<AccessControlClusterNotification> testNotifications = CollectionUtility.arrayList(new AccessControlClusterNotification());
    List<? extends Serializable> res = BEANS.get(NotificationCoalescer.class).coalesce(testNotifications);

    assertEquals(1, res.size());
    AccessControlClusterNotification firstNotification = (AccessControlClusterNotification) res.iterator().next();
    assertTrue(firstNotification.getUserIds().isEmpty());
  }

  /**
   * Tests that AccessControlClusterNotification are coalesced to one notification containing all users.
   */
  @Test
  public void testAccessControlCoalesce() {
    AccessControlClusterNotification n1 = new AccessControlClusterNotification(CollectionUtility.hashSet("user1", "user2"));
    AccessControlClusterNotification n2 = new AccessControlClusterNotification(CollectionUtility.hashSet("user1", "user3"));
    AccessControlClusterNotification n3 = new AccessControlClusterNotification();
    List<AccessControlClusterNotification> testNotifications = CollectionUtility.arrayList(n1, n2, n3);

    List<? extends Serializable> res = BEANS.get(NotificationCoalescer.class).coalesce(testNotifications);

    assertEquals(1, res.size());
    AccessControlClusterNotification firstNotification = (AccessControlClusterNotification) res.iterator().next();
    ScoutAssert.assertSetEquals(CollectionUtility.hashSet("user1", "user2", "user3"), firstNotification.getUserIds());
  }

  /**
   * Tests that notifications are coalesced, for a list of notifications with different types.
   */
  @Test
  public void testCoalesceDifferentKinds() {
    AccessControlClusterNotification n1 = new AccessControlClusterNotification(CollectionUtility.hashSet("user1", "user2"));
    AccessControlClusterNotification n2 = new AccessControlClusterNotification(CollectionUtility.hashSet("user1", "user3"));
    String n3 = "otherNotification";
    AccessControlClusterNotification n4 = new AccessControlClusterNotification();
    AccessControlClusterNotification n5 = new AccessControlClusterNotification();
    List<? extends Serializable> testNotifications = CollectionUtility.arrayList(n1, n2, n3, n4, n5);
    List<? extends Serializable> res = BEANS.get(NotificationCoalescer.class).coalesce(testNotifications);

    assertEquals(3, res.size());
    AccessControlClusterNotification firstNotification = (AccessControlClusterNotification) res.get(0);
    ScoutAssert.assertSetEquals(CollectionUtility.hashSet("user1", "user2", "user3"), firstNotification.getUserIds());
    assertEquals(n3, res.get(1));
    assertEquals(n4, res.get(2));
  }

}
