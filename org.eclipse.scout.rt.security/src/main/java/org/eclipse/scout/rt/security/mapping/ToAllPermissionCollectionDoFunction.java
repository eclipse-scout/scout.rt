/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security.mapping;

import org.eclipse.scout.rt.api.data.security.PermissionCollectionDo;
import org.eclipse.scout.rt.api.data.security.PermissionCollectionType;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.security.AllPermissionCollection;

@Order(4000)
public class ToAllPermissionCollectionDoFunction extends AbstractToPermissionCollectionDoFunction<AllPermissionCollection, PermissionCollectionDo> {

  @Override
  public void apply(AllPermissionCollection permissionCollection, PermissionCollectionDo permissionCollectionDo) {
    BEANS.get(ToPermissionCollectionDoFunction.class).apply(permissionCollection, permissionCollectionDo);
    permissionCollectionDo.withType(PermissionCollectionType.ALL);
  }
}
