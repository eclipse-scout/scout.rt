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

import java.security.AllPermission;
import java.security.Permission;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Stream;

public class AllPermissionCollection extends AbstractPermissionCollection {
  private static final long serialVersionUID = 1L;

  private final AllPermission m_allPermission = new AllPermission();

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
    return permission != null;
  }

  @Override
  public boolean implies(IPermission permission) {
    return permission != null;
  }

  @Override
  public PermissionLevel getGrantedPermissionLevel(IPermission permission) {
    return permission != null ? PermissionLevel.ALL : PermissionLevel.UNDEFINED;
  }

  @Override
  public Enumeration<Permission> elements() {
    return Collections.enumeration(Collections.singletonList(m_allPermission));
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
