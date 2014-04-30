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
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

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

  /**
   * Filters a given list of menus belonging to a specific value field by returning a list containing only valid
   * menus.
   * A menu is considered to be valid if at least the value field is enabled or if the menu does not inherit its
   * accessibility.
   * Additionally, the menu has either to be
   * <ul>
   * an empty space action and visible menu or
   * </ul>
   * <ul>
   * a single selection action and visible menu and the value field contains a non-null value
   * </ul>
   * The method prepareAction of a valid menu is executed if the parameter executePrepareAction is <code>true</code>
   * 
   * @since 4.0.0-M6
   */
  public static <T> List<IMenu> filterValidMenus(IValueField<T> valueField, List<IMenu> menusToFilter, boolean executePrepareAction) {
    T value = valueField.getValue();
    List<IMenu> filteredMenus = new ArrayList<IMenu>(menusToFilter.size());
    for (IMenu m : menusToFilter) {
      IMenu validMenu = null;
      if ((!m.isInheritAccessibility()) || valueField.isEnabled()) {
        if (m.isEmptySpaceAction()) {
          validMenu = m;
        }
        else if (m.isSingleSelectionAction()) {
          if (value != null) {
            validMenu = m;
          }
        }
      }
      if (validMenu != null) {
        if (executePrepareAction) {
          validMenu.prepareAction();
        }
        if (validMenu.isVisible()) {
          filteredMenus.add(validMenu);
        }
      }
    }
    return filteredMenus;
  }
}
