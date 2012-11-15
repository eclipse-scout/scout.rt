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

import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @since 3.4
 */
@SuppressWarnings("restriction")
public class UserPreferences extends EclipsePreferences {
  // cached values
  private IPath location;

  /**
   * Default constructor. Should only be called by #createExecutableExtension.
   */
  public UserPreferences(String qualifier) {
    super(null, qualifier);
  }

  @Override
  protected IPath getLocation() {
    if (location == null) {
      location = computeLocation(new Path(Platform.getUserLocation().getURL().getFile()), name());
    }
    return location;
  }

  public void create() {
    try {
      setLoading(true);
      loadLegacy();
      load();
      loaded();
      flush();
    }
    catch (BackingStoreException e) {
      e.printStackTrace();
    }
    finally {
      setLoading(false);
    }
  }

  @Override
  protected void loaded() {
    // ONLY in case the property set is empty load fom legacy instance scope
    if (keys().length == 0) {
      try {
        IEclipsePreferences oldPrefs = new InstanceScope().getNode(name());
        for (String key : oldPrefs.keys()) {
          String value = oldPrefs.get(key, null);
          if (value != null) {
            this.internalPut(key, value);
          }
        }
      }
      catch (BackingStoreException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected IEclipsePreferences getLoadLevel() {
    return this;
  }

  @Override
  protected EclipsePreferences internalCreate(EclipsePreferences nodeParent, String nodeName, Object context) {
    throw new UnsupportedOperationException();
  }
}
