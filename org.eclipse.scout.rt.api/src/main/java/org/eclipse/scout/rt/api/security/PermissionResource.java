/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.security;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.scout.rt.api.data.security.IToPermissionCollectionDoFunction;
import org.eclipse.scout.rt.api.data.security.PermissionCollectionDo;
import org.eclipse.scout.rt.dataobject.mapping.ToDoFunctionHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.IRestResource;
import org.eclipse.scout.rt.security.IAccessControlService;

@Path("permissions")
public class PermissionResource implements IRestResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PermissionCollectionDo getAllPermissions() {
    return BEANS.get(ToDoFunctionHelper.class).toDo(BEANS.get(IAccessControlService.class).getPermissions(), IToPermissionCollectionDoFunction.class);
  }
}
