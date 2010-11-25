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

import org.eclipse.scout.rt.shared.services.common.clientnotification.AbstractClientNotification;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

public class UserActivityClientNotification extends AbstractClientNotification {

  private static final long serialVersionUID = 1L;
  private final UserStatusMap m_map;

  public UserActivityClientNotification(UserStatusMap map) {
    m_map = map;
  }

  public boolean coalesce(IClientNotification existingNotification) {
    return existingNotification.getClass() == getClass();
  }

  public UserStatusMap getUserStatusMap() {
    return m_map;
  }

}
