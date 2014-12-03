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
package org.eclipse.scout.rt.server.services.common.clustersync;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the coalesce functionality of {@link IClusterNotification}s
 */
public abstract class AbstractClusterNotificationCoalesceTest<T extends IClusterNotification> {

  private T m_existingNotification;
  private T m_newNotification;
  private T m_newNonMergeableNotification;
  private IClusterNotification m_differenNotification; // has to be another notification than T to check coalesce with mixed Notifications

  public T getExistingNotification() {
    return m_existingNotification;
  }

  public T getNewNotification() {
    return m_newNotification;
  }

  public T getNewNonMegeableNotification() {
    return m_newNonMergeableNotification;
  }

  public IClusterNotification getDifferenNotification() {
    return m_differenNotification;
  }

  @Before
  public void before() {
    m_existingNotification = createExistingNotification();
    m_newNotification = createNewNotification();
    m_newNonMergeableNotification = createNewNonMergeableNotification();
    m_differenNotification = createDifferentNotification();
  }

  protected abstract T createExistingNotification();

  protected abstract T createNewNotification();

  protected abstract T createNewNonMergeableNotification();

  protected abstract IClusterNotification createDifferentNotification();

  @Test
  public void testCoalesce() {
    if (isCoalesceExpected()) {
      assertTrue(getExistingNotification().coalesce(getNewNotification()));
    }
    else {
      assertFalse(getExistingNotification().coalesce(getNewNotification()));
    }

    checkCoalesceResult(getExistingNotification());
  }

  protected abstract boolean isCoalesceExpected();

  protected abstract void checkCoalesceResult(T notificationToCheck);

  @Test
  public void testCoalesceFail() {
    assertFalse(getExistingNotification().coalesce(getNewNonMegeableNotification()));
    checkCoalesceFailResult(getExistingNotification());
  }

  protected abstract void checkCoalesceFailResult(T notificationToCheck);

  @Test
  public void testCoalesceWithDifferentNotification() {
    assertFalse(getExistingNotification().coalesce(getDifferenNotification()));

    checkCoalesceDifferentNotificationResult(getExistingNotification());
  }

  protected abstract void checkCoalesceDifferentNotificationResult(T notificationToCheck);
}
