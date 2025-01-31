/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
