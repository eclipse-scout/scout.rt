/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session;

import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.shared.session.ISessionService;

@ApplicationScoped
public interface IInitialSharedVariableContributor {

  /**
   * Contributor for client session variables initially loaded by {@link ISessionService}
   */
  void contribute(Map<String, Object> variables);
}
