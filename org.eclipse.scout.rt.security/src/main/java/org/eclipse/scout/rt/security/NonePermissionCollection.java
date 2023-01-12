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
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Stream;

public class NonePermissionCollection extends AbstractPermissionCollection {
  private static final long serialVersionUID = 1L;

  @Override
  public void add(Permission permission) {
    // ignore explicit add operation
  }

  @Override
  public void add(IPermission permission) {
    // ignore explicit add operation
  }

  @Override
  public boolean implies(Permission permission) {
    return false;
  }

  @Override
  public boolean implies(IPermission permission) {
    return false;
  }

  @Override
  public PermissionLevel getGrantedPermissionLevel(IPermission permission) {
    return permission != null ? PermissionLevel.NONE : PermissionLevel.UNDEFINED;
  }

  @Override
  public Enumeration<Permission> elements() {
    return Collections.enumeration(Collections.emptyList());
  }

  @Override
  public Stream<IPermission> stream() {
    return Stream.empty();
  }

  @Override
  public Stream<IPermission> stream(IPermission permission) {
    return Stream.empty();
  }
}
