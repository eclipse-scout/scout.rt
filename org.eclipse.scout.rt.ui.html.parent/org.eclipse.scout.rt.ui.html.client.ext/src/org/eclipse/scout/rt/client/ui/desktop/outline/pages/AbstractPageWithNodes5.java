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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.form.FormMenuType;
import org.eclipse.scout.rt.client.ui.form.IForm5;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.AbstractExtensiblePageWithNodes;

public class AbstractPageWithNodes5 extends AbstractExtensiblePageWithNodes {

  public AbstractPageWithNodes5() {
    super();
  }

  public AbstractPageWithNodes5(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * @deprecated Will be removed in the 6.0 Release.
   *             Use {@link #AbstractExtensiblePageWithNodes()} in combination with getter and setter (page variable)
   *             instead.
   */
  @Deprecated
  @SuppressWarnings("deprecation")
  public AbstractPageWithNodes5(org.eclipse.scout.rt.shared.ContextMap contextMap) {
    super(contextMap);
  }

  public AbstractPageWithNodes5(String userPreferenceContext) {
    super(userPreferenceContext);
  }

  public AbstractPageWithNodes5(boolean callInitializer, String userPreferenceContext) {
    super(callInitializer, userPreferenceContext);
  }

  @Override
  public void pageActivatedNotify() {
    super.pageActivatedNotify();

    if (getDetailForm() != null) {
      if (getDetailForm() instanceof IForm5) {
        //FIXME CGU requires adjustment in scout rt (removal of TablePageTreeMenuWrapper)
//        syncPageMenus((IForm5) getDetailForm());
      }
    }
  }

  protected void syncPageMenus(IForm5 form) {
    //Add page menus to the form which are not already there
    for (IMenu menu : getOutline().getContextMenu().getChildActions()) {
      if (!form.getContextMenu().getChildActions().contains(menu)) {
        Set<IMenuType> menuTypes = menu.getMenuTypes();
        menuTypes.add(FormMenuType.Regular);
        ((AbstractMenu) menu).setMenuTypes(menuTypes);
        form.getContextMenu().addChildAction(menu);
      }
    }

    //Get existing page menus on form
    List<IMenu> pageMenusOnForm = new LinkedList<IMenu>();
    for (IMenu menu : getOutline().getContextMenu().getChildActions()) {
      for (IMenuType menuType : menu.getMenuTypes()) {
        if (menuType instanceof TreeMenuType) {
          pageMenusOnForm.add(menu);
        }
      }
    }

    //Remove page menus which are not present (anymore) on the page (only necessary if the page menus may be dynamically added/removed)
    for (IMenu menu : pageMenusOnForm) {
      if (!getOutline().getContextMenu().getChildActions().contains(menu)) {
        form.getContextMenu().removeChildAction(menu);
      }
    }
  }
}
