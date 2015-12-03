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

import java.util.EventListener;

/**
 * Listener to be notified about changes in a preference node.
 * 
 * @since 5.1
 * @see IPreferences#addPreferenceChangeListener(IPreferenceChangeListener)
 */
public interface IPreferenceChangeListener extends EventListener {

  /**
   * Callback when the preferences of the observed preference node has been changed.
   *
   * @param event
   *          Event object containing the details about the change.
   */
  void preferenceChange(PreferenceChangeEvent event);

}
