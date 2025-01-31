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

import java.io.Serializable;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Extension to {@link PermissionCollection}
 *
 * @see IPermission
 * @see PermissionCollection
 * @see DefaultPermissionCollection
 */
@Bean
public interface IPermissionCollection {

  /**
   * @param permission
   *     if {@code null}, false is returned
   * @see PermissionCollection#implies(Permission)
   */
  boolean implies(Permission permission);

  /**
   * @param permission
   *     if {@code null}, false is returned
   * @see PermissionCollection#implies(Permission)
   */
  boolean implies(IPermission permission);

  /**
   * This methods returns the granted {@link PermissionLevel} for a given permission instance {@code permission}.
   * <ul>
   * <li>{@link PermissionLevel#UNDEFINED} if {@code permission} is {@code null} or in general 'not an
   * {@link IPermission}'
   * <li>{@link PermissionLevel#NONE} if in this permission collection no permission matches given permission
   * <li>{@link PermissionLevel} of unique matching granted permission in this permission collection
   * <li>{@link PermissionLevel} of all matching granted permission if they got the same permission level
   * <li>{@link PermissionLevel#UNDEFINED} if multiple granted permissions matches given permission with different
   * permission levels
   * </ul>
   *
   * @return non null {@link PermissionLevel}
   */
  PermissionLevel getGrantedPermissionLevel(IPermission permission);

  /**
   * @return stream with all assigned {@link IPermission}
   */
  Stream<IPermission> stream();

  /**
   * @return stream with {@link IPermission} for which holds {@link IPermission#matches(IPermission)}
   */
  Stream<IPermission> stream(IPermission permission);

  /**
   * @see PermissionCollection#add(Permission)
   */
  void add(Permission permission);

  /**
   * Prefer the method {@link #add(IPermission, PermissionLevel)} which sets the granted permission level.
   *
   * @see #add(IPermission, PermissionLevel)
   */
  void add(IPermission permission);

  /**
   * Adds permission to the collection and sets the grantedLevel
   */
  default void add(IPermission permission, PermissionLevel grantedLevel) {
    if (permission != null) {
      permission.setLevelInternal(grantedLevel);
      add(permission);
    }
  }

  /**
   * @see PermissionCollection#setReadOnly()
   */
  void setReadOnly();

  /**
   * @see PermissionCollection#isReadOnly()
   */
  boolean isReadOnly();

  /**
   * @return assigned custom value if any
   */
  <T extends Serializable> T getValue(Class<T> valueType);

  /**
   * @return all assigned custom values
   */
  Stream<Object> getValues();

  /**
   * Sets custom value assigned to the permission collection
   *
   * @param valueType
   *     not null
   * @param value
   *     may be null
   * @throws AssertionException
   *     if permission collection is read only
   */
  <T extends Serializable> void setValue(Class<T> valueType, T value);
}
