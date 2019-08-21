/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.security;

import java.security.Permission;
import java.security.PermissionCollection;

import org.eclipse.scout.rt.platform.Bean;

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
   *          if {@code null}, false is returned
   * @see PermissionCollection#implies(Permission)
   */
  boolean implies(Permission permission);

  /**
   * @param permission
   *          if {@code null}, false is returned
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
   * @param permission
   * @return non null {@link PermissionLevel}
   */
  PermissionLevel getGrantedPermissionLevel(IPermission permission);

  /**
   * @see PermissionCollection#add(Permission)
   */
  void add(Permission permission);

  /**
   * @see PermissionCollection#add(Permission)
   */
  void add(IPermission permission);

  /**
   * @see PermissionCollection#setReadOnly()
   */
  void setReadOnly();

  /**
   * @see PermissionCollection#isReadOnly()
   */
  boolean isReadOnly();

}
