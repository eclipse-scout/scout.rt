/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security;

import java.security.Permission;
import java.util.stream.Stream;

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.text.TEXTS;

@ApplicationScoped
public class AccessSupport {

  public boolean check(Permission p) {
    return BEANS.get(IAccessControlService.class).getPermissions().implies(p);
  }

  public void checkAndThrow(Permission p) {
    if (!check(p)) {
      throw getAccessCheckFailedException(p);
    }
  }

  public boolean checkAny(Permission... permissions) {
    if (permissions == null) {
      return false;
    }
    IPermissionCollection c = BEANS.get(IAccessControlService.class).getPermissions();
    return Stream.of(permissions).anyMatch(c::implies);
  }

  public void checkAnyAndThrow(Permission... permissions) {
    if (!checkAny(permissions)) {
      if (permissions == null || permissions.length == 0) {
        throw getDefaultAccessCheckFailedException();
      }
      else {
        throw getAccessCheckFailedException(permissions[0]);
      }
    }
  }

  public boolean checkAll(Permission... permissions) {
    if (permissions == null) {
      return false;
    }
    IPermissionCollection c = BEANS.get(IAccessControlService.class).getPermissions();
    return Stream.of(permissions).allMatch(c::implies);
  }

  public void checkAllAndThrow(Permission... permissions) {
    if (permissions == null || permissions.length == 0) {
      throw getDefaultAccessCheckFailedException();
    }
    IPermissionCollection c = BEANS.get(IAccessControlService.class).getPermissions();
    Stream.of(permissions).forEach(p -> {
      if (!c.implies(p)) {
        throw getAccessCheckFailedException(p);
      }
    });
  }

  public AccessForbiddenException getAccessCheckFailedException(Permission p) {
    if (p instanceof IPermission) {
      return getAccessCheckFailedException((IPermission) p);
    }
    return getDefaultAccessCheckFailedException()
        .withContextInfo("permission", "{}", p);
  }

  protected AccessForbiddenException getAccessCheckFailedException(IPermission p) {
    return new AccessForbiddenException(p.getAccessCheckFailedMessage())
        .withContextInfo("permission", "{}", p);
  }

  public AccessForbiddenException getDefaultAccessCheckFailedException() {
    return new AccessForbiddenException(TEXTS.get("YouAreNotAuthorizedToPerformThisAction"));
  }

  public PermissionLevel getGrantedPermissionLevel(IPermission permission) {
    return BEANS.get(IAccessControlService.class).getPermissions().getGrantedPermissionLevel(permission);
  }
}
