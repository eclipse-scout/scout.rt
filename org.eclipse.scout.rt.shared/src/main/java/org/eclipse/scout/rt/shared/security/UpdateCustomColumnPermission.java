/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.security;

import org.eclipse.scout.rt.security.AbstractPermission;

public class UpdateCustomColumnPermission extends AbstractPermission {
  private static final long serialVersionUID = 1L;

  public UpdateCustomColumnPermission() {
    super("scout.custom.column.update");
  }
}
