/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.prefs;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.service.IService;

/**
 * Service capable to get user preferences.
 *
 * @see Preferences#get(IClientSession, String)
 * @since 5.1
 */
public interface IUserPreferencesService extends IService {

  /**
   * Gets the {@link IPreferences} for the given <code>nodeId</code> and the given <code>userScope</code>
   *
   * @param userScope
   *     The {@link IClientSession} for which the settings should be retrieved. Must not be <code>null</code>.
   * @param nodeId
   *     The id of the node to retrieve. Must not be <code>null</code>.
   * @return The {@link IPreferences} for the given node and scope.
   * @throws ProcessingException
   *     On an error while loading the preferences.
   * @throws IllegalArgumentException
   *     if the session or nodeId is <code>null</code>.
   */
  IPreferences getPreferences(IClientSession userScope, String nodeId);
}
