/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.prefs;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Service capable to get user preferences.
 *
 * @since 5.1
 * @see Preferences#get(ISession, String)
 */
@FunctionalInterface
public interface IUserPreferencesService extends IService {

  /**
   * Gets the {@link IPreferences} for the given <code>nodeId</code> and the given <code>userScope</code>
   *
   * @param userScope
   *          The {@link ISession} for which the settings should be retrieved. Must not be <code>null</code>.
   * @param nodeId
   *          The id of the node to retrieve. Must not be <code>null</code>.
   * @return The {@link IPreferences} for the given node and scope.
   * @throws ProcessingException
   *           On an error while loading the preferences.
   * @throws IllegalArgumentException
   *           if the session or nodeId is <code>null</code>.
   */
  IPreferences getPreferences(ISession userScope, String nodeId);

}
