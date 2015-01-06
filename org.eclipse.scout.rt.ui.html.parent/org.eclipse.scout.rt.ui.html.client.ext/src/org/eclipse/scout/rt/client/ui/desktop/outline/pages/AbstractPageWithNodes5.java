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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.form.FormMenuType;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IForm5;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.AbstractExtensiblePageWithNodes;
import org.eclipse.scout.rt.shared.ui.menu.IMenu5;
import org.eclipse.scout.rt.shared.ui.menu.MenuWrapper;

public abstract class AbstractPageWithNodes5 extends AbstractExtensiblePageWithNodes {

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
  protected void ensureDetailFormCreated() throws ProcessingException {
    if (getDetailForm() != null) {
      return;
    }
    super.ensureDetailFormCreated();
    if (getDetailForm() instanceof IForm) {
      IForm5 form = (IForm5) getDetailForm();
      List<IMenu> menus = form.getContextMenu().getChildActions();
      adaptDetailFormMenus(menus);
      if (!CollectionUtility.equalsCollection(menus, form.getContextMenu().getChildActions())) {
        form.getContextMenu().setChildActions(menus);
      }
    }
  }

  protected void adaptDetailFormMenus(List<IMenu> menus) throws ProcessingException {
    List<IMenu> copy = new LinkedList<IMenu>(menus);
    // Remove system menus (ok cancel)
    for (IMenu menu : copy) {
      if (menu instanceof IMenu5 && ((IMenu5) menu).getSystemType() != IMenu5.SYSTEM_TYPE_NONE) {
        menus.remove(menu);
      }
    }

    // Add page menus to the form
    for (IMenu menu : getOutline().getContextMenu().getChildActions()) {
      // FIXME CGU improve this
      Set<IMenuType> types = new HashSet<IMenuType>();
      for (IMenuType type : menu.getMenuTypes()) {
        if (type instanceof FormMenuType) {
          types.add(type);
        }
      }
      if (types.isEmpty()) {
        types.add(FormMenuType.Regular);
      }
      menus.add(new MenuWrapper(menu, types));
    }
  }

}
