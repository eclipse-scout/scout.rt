/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.prefs;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.ISession;

/**
 * User preference service that is capable to persist preferences to a preference store.
 *
 * @see Preferences#get(ISession, String)
 * @since 5.1
 */
public interface IUserPreferencesStorageService extends IUserPreferencesService {

  /**
   * Flushes the given {@link IPreferences} to the persistent store.
   *
   * @param prefs
   *     The preferences to store.
   * @throws ProcessingException
   *     On an error while storing the given preferences.
   */
  void flush(IPreferences prefs);
}
