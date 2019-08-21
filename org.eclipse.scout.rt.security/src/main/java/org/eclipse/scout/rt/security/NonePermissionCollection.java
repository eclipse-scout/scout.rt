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
import java.util.Collections;
import java.util.Enumeration;

public class NonePermissionCollection extends AbstractPermissionCollection {
  private static final long serialVersionUID = 1L;

  @Override
  public void add(Permission permission) {
  }

  @Override
  public void add(IPermission permission) {
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
}
