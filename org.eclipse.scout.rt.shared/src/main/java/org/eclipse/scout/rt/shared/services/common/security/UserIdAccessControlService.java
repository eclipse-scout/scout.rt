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

import java.security.PermissionCollection;

import org.eclipse.scout.rt.shared.ISession;

/**
 * {@link IAccessControlService} service that uses {@link ISession#getUserId()} as internal cache key required by
 * {@link AbstractAccessControlService} implementation.
 * <p>
 * Extend this service at server side to load permission collection. It is <b>not</b> required to implement
 * {@link #execLoadPermissions(String)} at client side.
 */
public class UserIdAccessControlService extends AbstractAccessControlService<String> {

  @Override
  protected String getCurrentUserCacheKey() {
    return getUserIdOfCurrentUser();
  }

  @Override
  protected PermissionCollection execLoadPermissions(String userId) {
    return null;
  }
}
