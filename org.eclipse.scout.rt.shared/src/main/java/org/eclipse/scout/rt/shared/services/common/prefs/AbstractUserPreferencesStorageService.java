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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Abstract implementation to store the preferences on the session.
 *
 * @see Preferences#get(ISession, String)
 * @since 5.1
 */
public abstract class AbstractUserPreferencesStorageService implements IUserPreferencesStorageService {

  public static final String USER_PREF_KEY = "org.eclipse.scout.rt.shared.services.common.prefs";

  @Override
  @SuppressWarnings("unchecked")
  public IPreferences getPreferences(ISession session, String nodeId) {
    if (session == null) {
      throw new IllegalArgumentException("No user scope available.");
    }
    if (!StringUtility.hasText(nodeId)) {
      throw new IllegalArgumentException("No node ID available.");
    }
    synchronized (session) {
      Object userPrefs = session.getData(USER_PREF_KEY);
      Map<String, IPreferences> allUserPrefs = null;
      if (userPrefs instanceof Map) {
        allUserPrefs = (Map<String, IPreferences>) userPrefs;
      }
      else {
        allUserPrefs = new HashMap<>();
        session.setData(USER_PREF_KEY, allUserPrefs);
      }

      nodeId = nodeId.trim();
      IPreferences result = allUserPrefs.get(nodeId);
      if (result == null) {
        result = new Preferences(nodeId, session);
        load(getUserScope(session), nodeId, result);
        result.remove("eclipse.preferences.version"); // remove legacy prefs version key
        allUserPrefs.put(nodeId, result);
      }
      return result;
    }
  }

  /**
   * @return Gets the user scope identifier for the given session.
   */
  protected String getUserScope(ISession session) {
    String userId = session.getUserId();
    if (StringUtility.hasText(userId)) {
      return userId.trim();
    }
    return "anonymous";
  }

  protected abstract void load(String userScope, String nodeId, IPreferences prefsToFill);
}
