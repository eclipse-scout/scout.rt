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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotification;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the coalesce functionality of {@link IClusterNotification}s
 */
public abstract class AbstractClusterNotificationCoalesceTest<T extends IClusterNotification> {

  protected T m_existingNotification;
  protected T m_newNotification;
  protected IClusterNotification m_differenNotification; // has to be another notification than T to check coalesce with mixed Notifications

  public T getExistingNotification() {
    return m_existingNotification;
  }

  public T getNewNotification() {
    return m_newNotification;
  }

  public IClusterNotification getDifferenNotification() {
    return m_differenNotification;
  }

  @Before
  public void before() {
    m_existingNotification = createExistingNotification();
    m_newNotification = createNewNotification();
    m_differenNotification = createDifferentNotification();
  }

  abstract protected T createExistingNotification();

  abstract protected T createNewNotification();

  abstract protected IClusterNotification createDifferentNotification();

  @Test
  public void testCoalesce() {
    if (isCoalesceExpected()) {
      assertTrue(getExistingNotification().coalesce(getNewNotification()));
    }
    else {
      assertFalse(getExistingNotification().coalesce(getNewNotification()));
    }

    checkCoalesceResultSuccess(getExistingNotification());
  }

  protected abstract boolean isCoalesceExpected();

  abstract protected void checkCoalesceResultSuccess(T notificationToCheck);

  @Test
  public void testCoalesceFail() {

    assertFalse(getExistingNotification().coalesce(getDifferenNotification()));

    checkCoalesceResultFail(getExistingNotification());
  }

  abstract protected void checkCoalesceResultFail(T notificationToCheck);

}
