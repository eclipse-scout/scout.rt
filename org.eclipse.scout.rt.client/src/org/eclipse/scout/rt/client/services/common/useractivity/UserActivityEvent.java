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
package org.eclipse.scout.rt.client.services.common.useractivity;

import java.util.EventObject;

import org.eclipse.scout.rt.shared.services.common.useractivity.UserStatusMap;

public class UserActivityEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  private UserStatusMap m_userStatusMap;

  public UserActivityEvent(UserActivityManager source, UserStatusMap userStatusMap) {
    super(source);
    m_userStatusMap = userStatusMap;
  }

  @Override
  public UserActivityManager getSource() {
    return (UserActivityManager) super.getSource();
  }

  public UserStatusMap getUserStatusMap() {
    return m_userStatusMap;
  }

}
