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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.security.ServicePermission;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.scout.service.ServiceUtility;

/**
 * Title: BSI Products Description: BSI CASE generated code Copyright: Copyright
 * (c) 2001,2007 BSI AG
 * 
 * @version 3.x
 */

@SuppressWarnings("deprecation")
public final class ACCESS {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ACCESS.class);

  private ACCESS() {
  }

  /**
   * Conveniencefunction to check if the resource is accessible with that level
   * example: ACCESS.check(new
   * CompanyReadPermission(CompanyReadPermission.OWNED)) -> true
   */
  public static boolean check(Permission p) {
    return SERVICES.getService(IAccessControlService.class).checkPermission(p);
  }

  /**
   * Conveniencefunction to get the level of access on the resource example:
   * ACCESS.getLevel(new CompanyReadPermission()) -> CompanyReadPermission.ALL
   */
  public static int getLevel(Permission p) {
    return SERVICES.getService(IAccessControlService.class).getPermissionLevel(p);
  }

  /**
   * @deprecated never used
   */
  @Deprecated
  public static void checkService(Object service, String operation) throws ProcessingException {
    IAccessControlService acs = SERVICES.getService(IAccessControlService.class);
    if (acs != null) {
      Class c = service.getClass();
      for (Class i : ServiceUtility.getInterfacesHierarchy(c, Object.class)) {
        if (Object.class.isAssignableFrom(i)) {
          if (acs.checkPermission(new ServicePermission(i, operation))) {
            return;
          }
        }
      }
      throw new ProcessingException("service: " + service + ", operation: " + operation, new SecurityException("access denied"));
    }
  }

}
