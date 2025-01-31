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

import java.util.EventListener;

/**
 * Listener to be notified about changes in a preference node.
 *
 * @see IPreferences#addPreferenceChangeListener(IPreferenceChangeListener)
 * @since 5.1
 */
@FunctionalInterface
public interface IPreferenceChangeListener extends EventListener {

  /**
   * Callback when the preferences of the observed preference node has been changed.
   *
   * @param event
   *     Event object containing the details about the change.
   */
  void preferenceChange(PreferenceChangeEvent event);
}
