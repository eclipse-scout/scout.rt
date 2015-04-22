/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.useractivity;

import org.eclipse.scout.commons.TTLCache;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.service.AbstractService;
import org.eclipse.scout.rt.server.services.common.clientnotification.AllUserFilter;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.shared.services.common.useractivity.IUserActivityStateService;
import org.eclipse.scout.rt.shared.services.common.useractivity.UserActivityClientNotification;
import org.eclipse.scout.rt.shared.services.common.useractivity.UserStatusMap;

public abstract class AbstractUserActivityStateService extends AbstractService implements IUserActivityStateService {
  private final Object m_usersLock;
  private final TTLCache<Long, Integer> m_users;

  public AbstractUserActivityStateService() {
    m_usersLock = new Object();
    m_users = new TTLCache<>(60000L);
  }

  /**
   * Implements this method. This method normally calls {@link #setStatusImpl(long, int)}
   */
  @Override
  public abstract void setStatus(int status) throws ProcessingException;

  protected void setStatusImpl(long userId, int status) throws ProcessingException {
    int oldStatus = 0;
    synchronized (m_usersLock) {
      oldStatus = IUserActivityStateService.STATUS_OFFLINE;
      Integer oldStatInt = m_users.get(userId);
      if (oldStatInt != null) {
        oldStatus = m_users.get(userId);
      }
      m_users.put(userId, status);
      if (oldStatus != status) {
        UserStatusMap map = getUserStatusMap();
        BEANS.get(IClientNotificationService.class).putNotification(new UserActivityClientNotification(map), new AllUserFilter(AllUserFilter.DEFAULT_TIMEOUT));
      }
    }
  }

  @Override
  public UserStatusMap getUserStatusMap() {
    return new UserStatusMap(m_users.getEntries());
  }

}
