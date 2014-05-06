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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;

/**
 * Utility class for menus
 * 
 * @since 3.10.0-M4
 */
public final class MenuUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MenuUtility.class);

  private MenuUtility() {
  }

  /**
   * Collects all keyStrokes from an array of menus
   * 
   * @since 3.10.0-M4
   */
  public static List<IKeyStroke> getKeyStrokesFromMenus(List<? extends IMenu> menu) {
    Set<String> keys = new HashSet<String>();
    if (menu != null) {
      List<IKeyStroke> keyStrokes = new ArrayList<IKeyStroke>(menu.size());
      for (IMenu m : menu) {
        String s = m.getKeyStroke();
        if (StringUtility.hasText(s)) {
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
      return keyStrokes;
    }
    return CollectionUtility.emptyArrayList();
  }

  public static <T extends IActionNode<?>> boolean isVisible(T menu) {
    if (!menu.isVisible()) {
      return false;
    }
    if (menu.hasChildActions()) {
      boolean visible = false;
      for (Object o : menu.getChildActions()) {
        if (o instanceof IActionNode<?>) {
          IActionNode<?> m = (IActionNode<?>) o;
          if (!m.isSeparator() && m.isVisible()) {
            visible = true;
            break;
          }
        }
      }
      return visible;
    }
    return true;
  }

  /**
   * @param original
   * @return a list of all visible menus an eliminated multiple occurrences of separators.
   */
  public static <T extends IActionNode<?>> List<T> consolidateMenus(List<T> original) {
    LinkedList<T> consolidatedMenus = new LinkedList<T>();
    T lastMenu = null;
    for (T m : original) {
      if (isVisible(m)) {
        if (m.isSeparator()) {
          if (lastMenu != null && !lastMenu.isSeparator()) {
            consolidatedMenus.add(m);
          }
        }
        else {
          consolidatedMenus.add(m);
        }
        lastMenu = m;
      }
    }

    // remove tailing separators
    while (!consolidatedMenus.isEmpty() && consolidatedMenus.getLast().isSeparator()) {
        consolidatedMenus.removeLast();
    }
    return consolidatedMenus;
  }

}
