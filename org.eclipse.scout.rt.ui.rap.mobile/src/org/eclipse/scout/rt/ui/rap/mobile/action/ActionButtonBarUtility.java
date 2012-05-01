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
package org.eclipse.scout.rt.ui.rap.mobile.action;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;

/**
 * @since 3.8.0
 */
public class ActionButtonBarUtility {

  public static List<IMenu> convertButtonsToActions(IButton[] buttons) {
    List<IMenu> menuList = new LinkedList<IMenu>();
    for (IButton button : buttons) {
      IMenu action = convertButtonToAction(button);
      if (action != null) {
        menuList.add(action);
      }
    }

    return menuList;
  }

  public static IMenu convertButtonToAction(IButton button) {
    if (button == null) {
      return null;
    }

    return new ButtonWrappingAction(button);
  }

}
