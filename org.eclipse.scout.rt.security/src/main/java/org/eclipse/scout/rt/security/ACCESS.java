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

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * Helper class for checking permissions.
 */
public final class ACCESS {

  private ACCESS() {
  }

  /**
   * Checks permission against granted permissions of current user.
   * <p>
   * <code>
   * if(ACCESS.check(new ReadPersonPermission(personId))){ ... }
   * </code>
   *
   * @param permission
   *     if {@code null}, false is returned
   * @return true if access is granted
   */
  public static boolean check(Permission permission) {
    return BEANS.get(AccessSupport.class).check(permission);
  }

  /**
   * Throws an exception if {@link #check(Permission)} fails for the given permission.
   *
   * @throws AccessForbiddenException
   *     thrown if access is not granted
   * @see IPermission#getAccessCheckFailedMessage()
   */
  public static void checkAndThrow(Permission permission) {
    BEANS.get(AccessSupport.class).checkAndThrow(permission);
  }

  /**
   * @return true if access for any given permission is granted
   */
  public static boolean checkAny(Permission... permissions) {
    return BEANS.get(AccessSupport.class).checkAny(permissions);
  }

  /**
   * Throws an exception if {@link #checkAny(Permission...)} fails for the given permission.
   * <p>
   * The access check failed message is taken from the <em>first</em> permission.
   *
   * @throws AccessForbiddenException
   *     thrown if access is not granted
   * @see IPermission#getAccessCheckFailedMessage()
   */
  public static void checkAnyAndThrow(Permission... permissions) {
    BEANS.get(AccessSupport.class).checkAnyAndThrow(permissions);
  }

  /**
   * @return true if access for all given permission is granted
   */
  public static boolean checkAll(Permission... permissions) {
    return BEANS.get(AccessSupport.class).checkAll(permissions);
  }

  /**
   * Throws an exception if {@link #checkAll(Permission...)} fails for the given permission.
   * <p>
   * The access check failed message is taken from the <em>first</em> permission for which access was not granted.
   *
   * @throws AccessForbiddenException
   *     thrown if access is not granted
   * @see IPermission#getAccessCheckFailedMessage()
   */
  public static void checkAllAndThrow(Permission... permissions) {
    BEANS.get(AccessSupport.class).checkAllAndThrow(permissions);
  }

  /**
   * This methods returns the granted {@link PermissionLevel} for a given permission instance {@code permission}.
   * <ul>
   * <li>{@link PermissionLevel#UNDEFINED} if {@code permission} is {@code null} or in general 'not an
   * {@link IPermission}'
   * <li>{@link PermissionLevel#NONE} if no level at all is granted to {@code permission}
   * <li>{@link PermissionLevel} if the level can be determined exactly.
   * <li>{@link PermissionLevel#UNDEFINED} if there are multiple granted permission levels possible and there is not
   * enough data in the permission contained to determine the result more closer.
   * </ul>
   *
   * @return non null {@link PermissionLevel}
   * @see IPermissionCollection#getGrantedPermissionLevel(IPermission)
   */
  public static PermissionLevel getGrantedPermissionLevel(IPermission permission) {
    return BEANS.get(AccessSupport.class).getGrantedPermissionLevel(permission);
  }
}
