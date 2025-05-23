/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.session;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;

/**
 * Service supporting creation and handling of client sessions
 */
@TunnelToServer
public interface ISessionService extends IService {

  /**
   * Load initial shared variables on start of client session.
   */
  LoadInitialSharedVariablesResponse loadInitialSharedVariables();
}
