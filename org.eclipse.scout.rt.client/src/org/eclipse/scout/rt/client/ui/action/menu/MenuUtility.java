/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;

/**
 * Utility class for menus
 * 
 * @since 3.10.0-M4
 */
public final class MenuUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MenuUtility.class);

  /**
   * Collects all keyStrokes from an array of menus
   * 
   * @since 3.10.0-M4
   */
  public static List<IKeyStroke> getKeyStrokesFromMenus(List<? extends IMenu> menu) {
    Set<String> keys = new HashSet<String>();
    List<IKeyStroke> keyStrokes = new ArrayList<IKeyStroke>();
    if (menu != null) {
      for (IMenu m : menu) {
        String s = m.getKeyStroke();
        if (s != null && s.trim().length() > 0) {
          try {
            KeyStroke ks = new KeyStroke(s, m);
            ks.initAction();
            if (keys.add(ks.getKeyStroke())) {
              keyStrokes.add(ks);
            }
          }
          catch (ProcessingException e) {
            LOG.error("could not initialize enter key stroke.", e);
          }
        }
      }
    }
    return Collections.unmodifiableList(keyStrokes);
  }
}
