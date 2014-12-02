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
package org.eclipse.scout.rt.server.services.common.clustersync.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotification;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationMessageProperties;
import org.eclipse.scout.rt.server.services.common.code.UnloadCodeTypeCacheClusterNotification;
import org.eclipse.scout.rt.server.services.common.security.AccessControlCacheChangedClusterNotification;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ClusterNotificationMessage}
 */
public class ClusterNotificationMessageTest {

  private String m_originNode = "NODE 1";
  private String m_originUser = "OriginUser";
  private ClusterNotificationMessage m_oldMessage;
  private IClusterNotification m_oldNotification;
  private Set<String> m_oldUserIds;
  private IClusterNotificationMessageProperties m_oldProps;
  private ClusterNotificationMessage m_newMessage;
  private IClusterNotification m_newNotification;
  private Set<String> m_newUserIds;
  private IClusterNotificationMessageProperties m_newProps;
  private ClusterNotificationMessage m_differentMessage;
  private IClusterNotification m_differentNotification;
  private IClusterNotificationMessageProperties m_differentProps;

  @Before
  public void before() {
    m_oldUserIds = new HashSet<String>();
    m_oldUserIds.add("User1");
    m_oldUserIds.add("User2");
    m_oldNotification = new AccessControlCacheChangedClusterNotification(new HashSet<String>(m_oldUserIds));
    m_oldProps = new ClusterNotificationMessageProperties(m_originNode, m_originUser);
    m_oldMessage = new ClusterNotificationMessage(m_oldNotification, m_oldProps);

    m_newUserIds = new HashSet<String>();
    m_newUserIds.add("Person1");
    m_newUserIds.add("Person2");
    m_newNotification = new AccessControlCacheChangedClusterNotification(new HashSet<String>(m_newUserIds));
    m_newProps = new ClusterNotificationMessageProperties(m_originNode, m_originUser);
    m_newMessage = new ClusterNotificationMessage(m_newNotification, m_newProps);

    m_differentNotification = new UnloadCodeTypeCacheClusterNotification(null);
    m_differentProps = new ClusterNotificationMessageProperties(m_originNode, m_originUser);
    m_differentMessage = new ClusterNotificationMessage(m_differentNotification, m_differentProps);
  }

  @Test
  public void testCoalesce() {
    assertTrue(m_oldMessage.coalesce(m_newMessage));

    Set<String> userIds = new HashSet<String>(m_oldUserIds);
    userIds.addAll(m_newUserIds);
    Set<String> resultList = ((AccessControlCacheChangedClusterNotification) m_newMessage.getNotification()).getUserIds();
    assertEquals(userIds, resultList);
  }

  @Test
  public void testCoalesceFail() {
    assertFalse(m_differentMessage.coalesce(m_newMessage));
    Set<String> resultList = ((AccessControlCacheChangedClusterNotification) m_newMessage.getNotification()).getUserIds();
    assertEquals(m_newUserIds, resultList);

  }

}
