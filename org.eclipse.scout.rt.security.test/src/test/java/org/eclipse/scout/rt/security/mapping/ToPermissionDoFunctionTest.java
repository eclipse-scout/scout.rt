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

import org.eclipse.scout.rt.api.data.security.PermissionDo;
import org.eclipse.scout.rt.api.data.security.PermissionId;
import org.eclipse.scout.rt.dataobject.mapping.ToDoFunctionHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.security.AbstractPermission;
import org.eclipse.scout.rt.security.IPermission;
import org.junit.Test;

public class ToPermissionDoFunctionTest {

  @Test
  public void testToDoFunction() {
    ToDoFunctionHelper toDoFunctionHelper = BEANS.get(ToDoFunctionHelper.class);

    PermissionDo permissionDo = toDoFunctionHelper.toDo(null, IToPermissionDoFunction.class);
    assertNull(permissionDo);

    permissionDo = toDoFunctionHelper.toDo(permission(null), IToPermissionDoFunction.class);
    assertNotNull(permissionDo);
    assertEquals("Permission", permissionDo.getObjectType());
    assertNull(permissionDo.getId());

    PermissionId testPermissionId = PermissionId.of("test");
    permissionDo = toDoFunctionHelper.toDo(permission(testPermissionId), IToPermissionDoFunction.class);
    assertNotNull(permissionDo);
    assertEquals("Permission", permissionDo.getObjectType());
    assertEquals(testPermissionId, permissionDo.getId());
  }

  protected IPermission permission(PermissionId id) {
    return new AbstractPermission(id) {
      private static final long serialVersionUID = 1L;
    };
  }
}
