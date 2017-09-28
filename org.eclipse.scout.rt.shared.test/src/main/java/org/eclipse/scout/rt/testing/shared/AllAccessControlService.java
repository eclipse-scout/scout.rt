/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.shared;

import java.security.AllPermission;
import java.security.PermissionCollection;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.services.common.security.AbstractAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;

/**
 * {@link IAccessControlService} service for testing using {@link AllPermission}
 * <p>
 * This service is ignored by default and needs to be registered explicitly.
 */
@Order(4500)
public class AllAccessControlService extends AbstractAccessControlService<String> {

  @Override
  protected String getCurrentUserCacheKey() {
    return getUserIdOfCurrentUser();
  }

  @Override
  protected PermissionCollection execLoadPermissions(String userId) {
    AllPermission allPermission = new AllPermission();
    PermissionCollection permissionCollection = allPermission.newPermissionCollection();
    permissionCollection.add(allPermission);
    permissionCollection.setReadOnly();
    return permissionCollection;
  }
}
