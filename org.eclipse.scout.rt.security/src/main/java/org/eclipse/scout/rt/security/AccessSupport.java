/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AccessSupport {

  private static final Logger LOG = LoggerFactory.getLogger(AccessSupport.class);

  public boolean check(Permission p) {
    boolean implies = BEANS.get(IAccessControlService.class).getPermissions().implies(p);
    LOG.trace("check({}): {}", p, implies);
    return implies;
  }

  public void checkAndThrow(Permission p) {
    if (!check(p)) {
      LOG.debug("checkAndThrow(p) failed, throwing exception");
      throw getAccessCheckFailedException(p);
    }
  }

  public boolean checkAny(Permission... permissions) {
    if (permissions == null) {
      LOG.trace("checkAny(null) failed, no permissions supplied");
      return false;
    }
    IPermissionCollection c = BEANS.get(IAccessControlService.class).getPermissions();
    boolean anyImplies = Stream.of(permissions).anyMatch(c::implies);
    LOG.trace("checkAny({}): {}", permissions, anyImplies);
    return anyImplies;
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
      LOG.trace("checkAll(null) failed, no permissions supplied");
      return false;
    }
    IPermissionCollection c = BEANS.get(IAccessControlService.class).getPermissions();
    boolean allImplies = Stream.of(permissions).allMatch(c::implies);
    LOG.trace("checkAll({}): {}", permissions, allImplies);
    return allImplies;
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
    PermissionLevel grantedPermissionLevel = BEANS.get(IAccessControlService.class).getPermissions().getGrantedPermissionLevel(permission);
    LOG.trace("getGrantedPermissionLevel({}): {}", permission, grantedPermissionLevel);
    return grantedPermissionLevel;
  }
}
