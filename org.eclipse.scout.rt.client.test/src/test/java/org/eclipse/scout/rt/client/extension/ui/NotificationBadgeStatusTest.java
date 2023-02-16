/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui;

import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.junit.Before;
import org.junit.Test;

public class NotificationBadgeStatusTest {

  private NotificationBadgeStatus m_numericNotificationBadgeStatus;
  private NotificationBadgeStatus m_alphanumericNotificationBadgeStatus;
  private IStatus m_errorStatus;

  @Before
  public void setup() {
    m_numericNotificationBadgeStatus = new NotificationBadgeStatus("42");
    m_alphanumericNotificationBadgeStatus = new NotificationBadgeStatus("lorem ipsum dolor");
    m_errorStatus = new Status(IStatus.ERROR);
  }

  @Test
  public void testNotificationBadgeStatusCompare() {
    assertTrue(m_numericNotificationBadgeStatus.compareTo(m_alphanumericNotificationBadgeStatus) < 0);
    assertTrue(m_numericNotificationBadgeStatus.compareTo(m_errorStatus) < 0);
    assertTrue(m_alphanumericNotificationBadgeStatus.compareTo(m_errorStatus) < 0);
  }
}
