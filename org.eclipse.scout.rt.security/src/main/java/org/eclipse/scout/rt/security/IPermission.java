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

import org.eclipse.scout.rt.api.data.security.PermissionId;

/**
 * Extension to {@link Permission}
 * <p>
 * Unlike other permissions, a permission implementing this interface can only be implied by another {@link IPermission}
 * <b>with the same name (= the same id)</b>.
 *
 * @see Permission
 */
public interface IPermission {

  /**
   * Typed wrapper for {@link Permission#getName()}
   */
  PermissionId getId();

  /**
   * {@link PermissionLevel} which was granted within an {@link IPermissionCollection}.
   *
   * @return granted permission level or {@code null} if and only if this permission is not part of an
   *         {@link IPermissionCollection}
   */
  PermissionLevel getLevel();

  /**
   * A message which should be displayed to the user in case the permission check failed.
   * <p>
   * In most cases the following text keys are used
   * <ul>
   * <li>create YouAreNotAuthorizedToRegisterThisData
   * <li>read YouAreNotAllowedToReadThisData
   * <li>update YouAreNotAllowedToChangeThisData
   * <li>delete YouAreNotAllowedToDeleteThisData
   * <li>other YouAreNotAuthorizedToPerformThisAction
   * </ul>
   *
   * @return message
   */
  String getAccessCheckFailedMessage();

  /**
   * @see #implies(IPermission)
   */
  boolean implies(Permission permission);

  /**
   * Tests if access is granted according to this permission.
   * <p>
   * If {@link #matches(IPermission)} returns false then this method should return false too.
   *
   * @return true if the specified permission is implied by this permission
   * @see Permission#implies(Permission)
   */
  boolean implies(IPermission permission);

  /**
   * Tests if this permission is responsible for granting given permission.
   * <p>
   * If {@link #getId()} do not match, this method should return false.
   *
   * @return true if this permission is responsible for granting given permission
   */
  boolean matches(IPermission permission);

  /**
   * Internal method used during initialization any only used for permissions in a {@link IPermissionCollection}
   */
  void setLevelInternal(PermissionLevel level);

  /**
   * Internal method used during initialization of a {@link IPermissionCollection}
   * <p>
   * Validates and assigns permission collection instance to this permission
   *
   * @see DefaultPermissionCollection
   */
  void assignPermissionCollection(IPermissionCollection permissionCollection);

}
