/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.security;

import java.security.Permission;

import org.eclipse.scout.service.SERVICES;

/**
 * Helper class for checking permissions.
 * 
 * @version 3.x
 */
public final class ACCESS {

  private ACCESS() {
  }

  /**
   * Convenience function to check if the resource is accessible with that level
   * example: ACCESS.check(new
   * CompanyReadPermission(CompanyReadPermission.OWNED)) -> true
   */
  public static boolean check(Permission p) {
    return SERVICES.getService(IAccessControlService.class).checkPermission(p);
  }

  /**
   * Convenience function to get the level of access on the resource example:
   * ACCESS.getLevel(new CompanyReadPermission()) -> CompanyReadPermission.ALL
   */
  public static int getLevel(Permission p) {
    return SERVICES.getService(IAccessControlService.class).getPermissionLevel(p);
  }

}
