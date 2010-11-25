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
package org.eclipse.scout.commons.prefs;

import java.util.HashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * Object representing the user scope in the Eclipse preferences, located under
 * the user area.
 * <p>
 * This is useful when multiple users are working on the same eclipse instance under same os user account.
 * <p>
 * 
 * @since 3.4
 * @noextend This class is not intended to be subclassed by clients.
 */
public final class UserScope {
  public static final String SCOPE = "user"; //$NON-NLS-1$
  private static HashMap<String, UserPreferences> prefsMap;
  private static Object prefsMapLock;

  static {
    prefsMapLock = new Object();
    prefsMap = new HashMap<String, UserPreferences>();
  }

  /**
   * Create and return a new instance scope instance.
   */
  public UserScope() {
  }

  /*
   * @see org.eclipse.core.runtime.preferences.IScopeContext#getLocation()
   */
  public IPath getLocation() {
    // Return null. The user location usually corresponds to the state
    // location of the bundle and we don't know what bundle we are dealing with.
    return new Path(Platform.getUserLocation().getURL().getFile());
  }

  /*
   * @see org.eclipse.core.runtime.preferences.IScopeContext#getName()
   */
  public String getName() {
    return SCOPE;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.eclipse.core.runtime.preferences.IScopeContext#getNode(java.lang.String
   * )
   */
  public IEclipsePreferences getNode(String qualifier) {
    synchronized (prefsMapLock) {
      UserPreferences prefs = prefsMap.get(qualifier);
      if (prefs == null) {
        prefs = new UserPreferences(qualifier);
        prefs.create();
        //
        prefsMap.put(qualifier, prefs);
      }
      return prefs;
    }
  }
}
