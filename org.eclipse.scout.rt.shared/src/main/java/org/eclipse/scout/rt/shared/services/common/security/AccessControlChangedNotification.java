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
package org.eclipse.scout.rt.shared.services.common.security;

import java.security.Permissions;

import org.eclipse.scout.rt.shared.cache.ICache;

/**
 * Notification is sent from server to client to notify that the permission set has changed
 *
 * @deprecated Will be removed in Scout 6.1. See {@link ICache}
 */
@SuppressWarnings("deprecation")
@Deprecated
public class AccessControlChangedNotification implements IAccessControlNotification {
  private static final long serialVersionUID = 1L;

  private final Permissions m_permissions;

  public AccessControlChangedNotification(Permissions permissions) {
    m_permissions = permissions;
  }

  public Permissions getPermissions() {
    return m_permissions;
  }

}
