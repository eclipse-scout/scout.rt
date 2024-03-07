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

import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.api.data.security.PermissionCollectionDo;
import org.eclipse.scout.rt.api.data.security.PermissionCollectionType;
import org.eclipse.scout.rt.api.data.security.PermissionDo;
import org.eclipse.scout.rt.dataobject.mapping.ToDoFunctionHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.security.IPermissionCollection;

public class ToPermissionCollectionDoFunction extends AbstractToPermissionCollectionDoFunction<IPermissionCollection, PermissionCollectionDo> {

  protected static final String PERMISSION_COLLECTION_OBJECT_TYPE = "PermissionCollection";

  @Override
  public void apply(IPermissionCollection permissionCollection, PermissionCollectionDo permissionCollectionDo) {
    ToDoFunctionHelper toDoFunctionHelper = BEANS.get(ToDoFunctionHelper.class);
    permissionCollectionDo
        .withObjectType(PERMISSION_COLLECTION_OBJECT_TYPE)
        .withPermissions(permissionCollection.stream()
            .map(permission -> toDoFunctionHelper.toDo(permission, IToPermissionDoFunction.class))
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(PermissionDo::getName, Collectors.toSet())))
        .withType(PermissionCollectionType.DEFAULT);
  }
}
