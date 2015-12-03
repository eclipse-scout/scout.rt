/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.security;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;

/**
 * Support service to explicitly close a session and release cached sessions, resources, and credentials
 */
@TunnelToServer
public interface ILogoutService extends IService {

  /**
   * calling this remote service on the server causes the session to be closed and resources to be cleaned, a new
   * session and new login will be needed to continue working
   */
  void logout();

}
