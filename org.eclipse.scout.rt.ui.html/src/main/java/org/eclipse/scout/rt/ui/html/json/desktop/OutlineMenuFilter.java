/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;

public class OutlineMenuFilter<T extends IAction> extends DisplayableActionFilter<T> {

  @Override
  public boolean test(T element) {
    if (super.test(element) && element instanceof IMenu) {
      return ((IMenu) element).getMenuTypes().contains(TreeMenuType.Header);
    }
    return false;
  }
}
