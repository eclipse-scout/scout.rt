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

import java.util.HashMap;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;

/**
 * Utility class for menus
 * 
 * @since 3.10.0-M4
 */
public final class MenuUtility {
  /**
   * Collects all keyStrokes from an array of menus
   * 
   * @since 3.10.0-M4
   */
  public static IKeyStroke[] getKeyStrokesFromMenus(IMenu[] menu) {
    HashMap<String, IKeyStroke> ksMap = new HashMap<String, IKeyStroke>();
    if (menu != null) {
      for (IMenu m : menu) {
        String s = m.getKeyStroke();
        if (s != null && s.trim().length() > 0) {
          KeyStroke ks = new KeyStroke(s, m);
          ksMap.put(ks.getKeyStroke().toUpperCase(), ks);
        }
      }
    }
    return ksMap.values().toArray(new IKeyStroke[ksMap.size()]);
  }
}
