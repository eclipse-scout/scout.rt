/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.exception;

import java.security.Permission;

import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public interface IPermissionCodeHelper {

  /**
   * Returns a hashed permission code to be shown on an error message
   */
  String getPermissionCode(Permission permission);
}
