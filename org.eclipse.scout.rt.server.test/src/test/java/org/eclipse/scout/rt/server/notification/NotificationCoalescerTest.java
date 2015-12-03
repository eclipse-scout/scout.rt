/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.ScoutAssert;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link NotificationCoalescer}
 */
@RunWith(PlatformTestRunner.class)
public class NotificationCoalescerTest {

  private List<IBean<?>> m_registerServices;

  @Before
  public void setUp() throws Exception {
    // register new test coalescer
    m_registerServices = TestingUtility.registerBeans(
        new BeanMetaData(ICoalescer.class).
            withInitialInstance(BeanInstanceUtil.create(P_TestNotificationCoalescer.class)).
            withApplicationScoped(true));

    // now rebuild coalescer linking
    BEANS.get(NotificationCoalescer.class).buildCoalescerLinking();
  }

  @After
  public void tearDown() throws Exception {
    TestingUtility.unregisterBeans(m_registerServices);
  }

  @Test
  public void testCoalesceEmptySet() {
    List<? extends Serializable> res = BEANS.get(NotificationCoalescer.class).coalesce(new ArrayList<Serializable>());
    assertTrue(res.isEmpty());
  }

  @Test
  public void testCoalesce_single() throws Exception {
    List<P_TestNotification> testNotifications = CollectionUtility.arrayList(new P_TestNotification());
    List<? extends Serializable> res = BEANS.get(NotificationCoalescer.class).coalesce(testNotifications);

    assertEquals(1, res.size());
    P_TestNotification firstNotification = (P_TestNotification) res.iterator().next();
    assertTrue(firstNotification.getTestIds().isEmpty());
  }

  /**
   * Tests that {@link P_TestNotification} are coalesced to one notification containing all test ids.
   */
  @Test
  public void testCoalesce() {
    P_TestNotification n1 = new P_TestNotification(CollectionUtility.hashSet("1", "2"));
    P_TestNotification n2 = new P_TestNotification(CollectionUtility.hashSet("1", "3"));
    P_TestNotification n3 = new P_TestNotification();
    List<P_TestNotification> testNotifications = CollectionUtility.arrayList(n1, n2, n3);

    List<? extends Serializable> res = BEANS.get(NotificationCoalescer.class).coalesce(testNotifications);

    assertEquals(1, res.size());
    P_TestNotification firstNotification = (P_TestNotification) res.iterator().next();
    ScoutAssert.assertSetEquals(CollectionUtility.hashSet("1", "2", "3"), firstNotification.getTestIds());
  }

  /**
   * Tests that notifications are coalesced, for a list of notifications with different types.
   */
  @Test
  public void testCoalesceDifferentKinds() {
    P_TestNotification n1 = new P_TestNotification(CollectionUtility.hashSet("1", "2"));
    P_TestNotification n2 = new P_TestNotification(CollectionUtility.hashSet("1", "3"));
    String n3 = "otherNotification";
    P_TestNotification n4 = new P_TestNotification();
    P_TestNotification n5 = new P_TestNotification();
    List<? extends Serializable> testNotifications = CollectionUtility.arrayList(n1, n2, n3, n4, n5);
    List<? extends Serializable> res = BEANS.get(NotificationCoalescer.class).coalesce(testNotifications);

    assertEquals(3, res.size());
    P_TestNotification firstNotification = (P_TestNotification) res.get(0);
    ScoutAssert.assertSetEquals(CollectionUtility.hashSet("1", "2", "3"), firstNotification.getTestIds());
    assertEquals(n3, res.get(1));
    assertEquals(n4, res.get(2));
  }

  private static class P_TestNotification implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Set<String> m_testIds;

    public P_TestNotification() {
      this(null);
    }

    public P_TestNotification(Collection<String> testIds) {
      m_testIds = CollectionUtility.hashSetWithoutNullElements(testIds);
    }

    public Set<String> getTestIds() {
      return m_testIds;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((m_testIds == null) ? 0 : m_testIds.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      P_TestNotification other = (P_TestNotification) obj;
      if (m_testIds == null) {
        if (other.m_testIds != null) {
          return false;
        }
      }
      else if (!m_testIds.equals(other.m_testIds)) {
        return false;
      }
      return true;
    }
  }

  private static class P_TestNotificationCoalescer implements ICoalescer<P_TestNotification> {

    @Override
    public List<P_TestNotification> coalesce(List<P_TestNotification> notifications) {
      if (notifications.isEmpty()) {
        return CollectionUtility.emptyArrayList();
      }

      Set<String> testIds = new HashSet<>();
      for (P_TestNotification notification : notifications) {
        testIds.addAll(notification.getTestIds());
      }
      return CollectionUtility.arrayList(new P_TestNotification(testIds));
    }
  }
}
