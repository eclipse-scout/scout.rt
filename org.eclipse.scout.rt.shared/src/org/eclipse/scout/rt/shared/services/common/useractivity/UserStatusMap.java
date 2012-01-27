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
package org.eclipse.scout.rt.shared.services.common.useractivity;

import java.io.Serializable;
import java.util.Map;

public class UserStatusMap implements Serializable {
  private static final long serialVersionUID = 1L;

  protected Map<Long, Integer> /* userId, Status */m_users;

  public UserStatusMap(Map<Long, Integer> users) {
    m_users = users;
  }

  public Map<Long, Integer> getMap() {
    return m_users;
  }

  public int getStatus(long userId) {
    int status = IUserActivityStateService.STATUS_OFFLINE;
    Integer s = m_users.get(userId);
    if (s != null) {
      status = s.intValue();
    }
    return status;
  }

}
