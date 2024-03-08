/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.security;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.RuntimeDelegate;

import org.eclipse.scout.rt.dataobject.mapping.ToDoFunctionHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.IRestResource;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.security.mapping.IToPermissionCollectionDoFunction;

@Path("permissions")
public class PermissionResource implements IRestResource {

  @GET
  public Response getAllPermissions(@Context Request request) {
    var permissionCollection = BEANS.get(IAccessControlService.class).getPermissions();
    var permissionCollectionDo = BEANS.get(ToDoFunctionHelper.class).toDo(permissionCollection, IToPermissionCollectionDoFunction.class);

    // compute hashCode on DO because the permissionCollection has no hashCode implementation
    // and etag would therefore not match on a cache invalidate with the same resulting permissions on a new collection instance.
    var etag = RuntimeDelegate.getInstance()
        .createHeaderDelegate(EntityTag.class)
        .fromString("W/\"" + permissionCollectionDo.hashCode() + "\"");
    var responseBuilder = request.evaluatePreconditions(etag);
    if (responseBuilder != null) {
      // 304 Not Modified (client's cached version is still up-to-date)
      return responseBuilder.build();
    }

    var cc = new CacheControl();
    cc.setPrivate(true);
    cc.setMustRevalidate(true);

    return Response.ok()
        .entity(permissionCollectionDo)
        .type(MediaType.APPLICATION_JSON)
        .tag(etag)
        .cacheControl(cc)
        .build();
  }
}
