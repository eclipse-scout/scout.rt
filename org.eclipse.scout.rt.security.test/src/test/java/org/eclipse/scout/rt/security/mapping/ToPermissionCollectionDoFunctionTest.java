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

import static org.junit.Assert.*;

import java.util.Set;

import org.eclipse.scout.rt.api.data.security.PermissionCollectionDo;
import org.eclipse.scout.rt.api.data.security.PermissionCollectionType;
import org.eclipse.scout.rt.api.data.security.PermissionId;
import org.eclipse.scout.rt.dataobject.mapping.ToDoFunctionHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.security.AbstractPermission;
import org.eclipse.scout.rt.security.AllPermissionCollection;
import org.eclipse.scout.rt.security.DefaultPermissionCollection;
import org.eclipse.scout.rt.security.IPermission;
import org.eclipse.scout.rt.security.IPermissionCollection;
import org.eclipse.scout.rt.security.NonePermissionCollection;
import org.junit.Test;

public class ToPermissionCollectionDoFunctionTest {

  @Test
  public void testToDoFunction() {
    ToDoFunctionHelper toDoFunctionHelper = BEANS.get(ToDoFunctionHelper.class);

    PermissionCollectionDo permissionCollectionDo = toDoFunctionHelper.toDo(null, IToPermissionCollectionDoFunction.class);
    assertNull(permissionCollectionDo);

    permissionCollectionDo = toDoFunctionHelper.toDo(permissionCollection(), IToPermissionCollectionDoFunction.class);
    assertNotNull(permissionCollectionDo);
    assertEquals("PermissionCollection", permissionCollectionDo.getObjectType());
    assertEquals(PermissionCollectionType.DEFAULT, permissionCollectionDo.getType());
    assertEquals(0, permissionCollectionDo.getPermissions().size());

    PermissionId test1PermissionId = PermissionId.of("test1");
    PermissionId test2PermissionId = PermissionId.of("test2");
    permissionCollectionDo = toDoFunctionHelper.toDo(permissionCollection(permission(test1PermissionId), permission(test2PermissionId)), IToPermissionCollectionDoFunction.class);
    assertNotNull(permissionCollectionDo);
    assertEquals("PermissionCollection", permissionCollectionDo.getObjectType());
    assertEquals(PermissionCollectionType.DEFAULT, permissionCollectionDo.getType());
    assertEquals(Set.of(test1PermissionId, test2PermissionId), permissionCollectionDo.getPermissions().keySet());

    permissionCollectionDo = toDoFunctionHelper.toDo(BEANS.get(AllPermissionCollection.class), IToPermissionCollectionDoFunction.class);
    assertNotNull(permissionCollectionDo);
    assertEquals("PermissionCollection", permissionCollectionDo.getObjectType());
    assertEquals(PermissionCollectionType.ALL, permissionCollectionDo.getType());

    permissionCollectionDo = toDoFunctionHelper.toDo(BEANS.get(NonePermissionCollection.class), IToPermissionCollectionDoFunction.class);
    assertNotNull(permissionCollectionDo);
    assertEquals("PermissionCollection", permissionCollectionDo.getObjectType());
    assertEquals(PermissionCollectionType.NONE, permissionCollectionDo.getType());
  }

  protected IPermission permission(PermissionId id) {
    return new AbstractPermission(id) {
      private static final long serialVersionUID = 1L;
    };
  }

  protected IPermissionCollection permissionCollection(IPermission... permissions) {
    IPermissionCollection permissionCollection = BEANS.get(DefaultPermissionCollection.class);
    for (IPermission permission : permissions) {
      if (permission != null) {
        permissionCollection.add(permission);
      }
    }
    return permissionCollection;
  }
}
