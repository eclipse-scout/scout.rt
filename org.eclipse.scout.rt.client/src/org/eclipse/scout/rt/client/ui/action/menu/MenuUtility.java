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
import java.util.HashMap;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;

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

  /**
   * Filters a given array of menus belonging to a specific value field by returning an array containing only valid
   * menus.
   * A menu is considered to be valid if at least the value field is enabled or if the menu does not inherit its
   * accessibility.
   * Additionally, the menu has either to be
   * <ul>
   * <li>an empty space action and visible menu or</li>
   * <li>a single selection action and visible menu and the value field contains a non-null value</li>
   * </ul>
   * The method prepareAction of a valid menu is executed if the parameter executePrepareAction is <code>true</code>
   * 
   * @since 4.0.0-M6
   */
  public static <T> IMenu[] filterValidMenus(IValueField<T> valueField, IMenu[] menusToFilter, boolean executePrepareAction) {
    T value = valueField.getValue();
    ArrayList<IMenu> filteredMenus = new ArrayList<IMenu>();
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
    return filteredMenus.toArray(new IMenu[0]);
  }

  /**
   * Filters a given array of menus belonging to a button by returning an array containing only valid
   * menus. A menu is considered to be valid if the menu is visible.
   * The method prepareAction of a valid menu is executed if the parameter executePrepareAction is <code>true</code>
   * 
   * @since 4.0.0-M6
   */
  public static IMenu[] filterValidMenusOnButton(IButton button, IMenu[] menusToFilter, boolean executePrepareAction) {
    ArrayList<IMenu> filteredMenus = new ArrayList<IMenu>();
    for (IMenu menu : menusToFilter) {
      if (executePrepareAction) {
        menu.prepareAction();
      }
      if (menu.isVisible()) {
        filteredMenus.add(menu);
      }
    }
    return filteredMenus.toArray(new IMenu[0]);
  }
}
