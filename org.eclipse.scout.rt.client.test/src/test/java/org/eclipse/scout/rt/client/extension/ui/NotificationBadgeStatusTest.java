/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
