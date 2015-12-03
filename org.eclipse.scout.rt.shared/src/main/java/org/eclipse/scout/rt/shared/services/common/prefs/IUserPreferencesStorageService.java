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
package org.eclipse.scout.rt.shared.services.common.prefs;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.ISession;

/**
 * User preference service that is capable to persist preferences to a preference store.
 *
 * @since 5.1
 * @see Preferences#get(ISession, String)
 */
public interface IUserPreferencesStorageService extends IUserPreferencesService {

  /**
   * Flushes the given {@link IPreferences} to the persistent store.
   *
   * @param prefs
   *          The preferences to store.
   * @throws ProcessingException
   *           On an error while storing the given preferences.
   */
  void flush(IPreferences prefs);

}
