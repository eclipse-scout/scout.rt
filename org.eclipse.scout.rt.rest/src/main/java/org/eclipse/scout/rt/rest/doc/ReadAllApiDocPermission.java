/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.doc;

import org.eclipse.scout.rt.api.data.security.PermissionId;
import org.eclipse.scout.rt.security.AbstractPermission;

public class ReadAllApiDocPermission extends AbstractPermission {
  private static final long serialVersionUID = 1L;
  public static final PermissionId ID = PermissionId.of("scout.api.doc.read.all");

  public ReadAllApiDocPermission() {
    super(ID);
  }
}
