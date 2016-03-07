/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;

public class OutlineContextMenuFilter<T extends IMenu> extends DisplayableActionFilter<T> {

  @Override
  public boolean accept(T element) {
    if (!super.accept(element)) {
      return false;
    }
    List<IMenu> childActions = element.getChildActions();
    for (IMenu childAction : childActions) {
      if (childAction.getMenuTypes().contains(TreeMenuType.Header) && childAction.isVisibleGranted()) {
        return true;
      }
    }
    return false;
  }

}
