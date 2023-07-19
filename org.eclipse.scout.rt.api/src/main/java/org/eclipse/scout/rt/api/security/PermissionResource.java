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
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.eclipse.scout.rt.api.data.security.IToPermissionCollectionDoFunction;
import org.eclipse.scout.rt.dataobject.mapping.ToDoFunctionHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.IRestResource;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.security.IPermissionCollection;

@Path("permissions")
public class PermissionResource implements IRestResource {

  @GET
  public Response getAllPermissions(@Context Request request) {
    IPermissionCollection permissionCollection = BEANS.get(IAccessControlService.class).getPermissions();

    EntityTag etag = EntityTag.valueOf("W/\"" + permissionCollection.hashCode() + "\"");
    ResponseBuilder responseBuilder = request.evaluatePreconditions(etag);
    if (responseBuilder != null) {
      // 304 Not Modified (client's cached version is still up-to-date)
      return responseBuilder.build();
    }

    CacheControl cc = new CacheControl();
    cc.setPrivate(true);
    cc.setMustRevalidate(true);

    return Response.ok()
        .entity(BEANS.get(ToDoFunctionHelper.class).toDo(permissionCollection, IToPermissionCollectionDoFunction.class))
        .type(MediaType.APPLICATION_JSON)
        .tag(etag)
        .cacheControl(cc)
        .build();
  }
}
