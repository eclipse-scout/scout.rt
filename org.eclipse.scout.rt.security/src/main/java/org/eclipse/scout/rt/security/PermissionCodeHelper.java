/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security;

import java.nio.charset.StandardCharsets;
import java.security.Permission;

import org.eclipse.scout.rt.api.data.security.PermissionId;
import org.eclipse.scout.rt.dataobject.exception.IPermissionCodeHelper;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.HexUtility;

/**
 * The permission code is calculated by hashing the permission name using {@link SecurityUtility#hash}
 * and then encoding it to hex and shortening it.
 */
public class PermissionCodeHelper implements IPermissionCodeHelper {

  @Override
  public String getPermissionCode(Permission permission) {
    if (permission == null) {
      return null;
    }
    if (permission instanceof IPermission p) {
      return getPermissionCode(p);
    }
    return hash(permission.getName());
  }

  protected String getPermissionCode(IPermission permission) {
    return hashPermissionId(permission.getId());
  }

  protected String hashPermissionId(PermissionId permissionId) {
    return hash(permissionId.unwrap());
  }

  protected String hash(String text) {
    if (text == null) {
      return null;
    }
    return encodeAndShorten(SecurityUtility.hash(text.getBytes(StandardCharsets.UTF_8)));
  }

  protected String encodeAndShorten(byte[] hash) {
    return HexUtility.encode(hash).substring(0, 10);
  }
}
