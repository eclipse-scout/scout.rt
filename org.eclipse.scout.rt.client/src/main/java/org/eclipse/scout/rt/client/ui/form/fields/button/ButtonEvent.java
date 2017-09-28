/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.button;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@SuppressWarnings({"serial", "squid:S2057"})
public class ButtonEvent extends EventObject implements IModelEvent {
  /**
   * event when button was clicked
   */
  public static final int TYPE_CLICKED = 1;
  /**
   * event to ui to request showing the (dropdown) menu popup
   */
  public static final int TYPE_REQUEST_POPUP = 4;

  private final int m_type;
  private List<IMenu> m_popupMenuList;

  public ButtonEvent(IButton source, int type) {
    super(source);
    m_type = type;
  }

  public IButton getButton() {
    return (IButton) super.getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }

  public void addPopupMenus(List<IMenu> menus) {
    if (menus == null || menus.isEmpty()) {
      return;
    }
    if (m_popupMenuList == null) {
      m_popupMenuList = new ArrayList<>(menus.size());
    }
    m_popupMenuList.addAll(menus);
  }

  public void addPopupMenu(IMenu menu) {
    if (menu == null) {
      return;
    }
    if (m_popupMenuList == null) {
      m_popupMenuList = new ArrayList<>();
    }
    m_popupMenuList.add(menu);
  }

  public List<IMenu> getPopupMenus() {
    return CollectionUtility.arrayList(m_popupMenuList);
  }
}
