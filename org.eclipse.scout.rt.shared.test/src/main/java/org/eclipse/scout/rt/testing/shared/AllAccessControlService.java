/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.shared;

import java.security.AllPermission;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.security.User;
import org.eclipse.scout.rt.security.AbstractAccessControlService;
import org.eclipse.scout.rt.security.AllPermissionCollection;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.security.IPermissionCollection;

/**
 * {@link IAccessControlService} service for testing using {@link AllPermission}
 */
@Order(4500)
public class AllAccessControlService extends AbstractAccessControlService {

  @Override
  public IPermissionCollection getPermissions() {
    return BEANS.get(AllPermissionCollection.class);
  }

  @Override
  protected IPermissionCollection execLoadPermissions(User user) {
    throw new UnsupportedOperationException();
  }
}
