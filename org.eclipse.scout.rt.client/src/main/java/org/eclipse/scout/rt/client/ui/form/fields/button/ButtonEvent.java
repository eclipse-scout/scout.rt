/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

public class ButtonEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;
  /**
   * event when button was clicked
   */
  public static final int TYPE_CLICKED = 1;
  /**
   * In case button was pressed and armed, this event sets armed=false and prevents the button from firing an action
   */
  public static final int TYPE_DISARM = 3;
  /**
   * event to ui to request showing the (dropdown) menu popup
   */
  public static final int TYPE_REQUEST_POPUP = 4;

  private final int m_type;
  private ArrayList<IMenu> m_popupMenuList;

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
    if (m_popupMenuList == null) {
      m_popupMenuList = new ArrayList<IMenu>();
    }
    m_popupMenuList.addAll(menus);
  }

  public void addPopupMenu(IMenu menu) {
    if (m_popupMenuList == null) {
      m_popupMenuList = new ArrayList<IMenu>();
    }
    m_popupMenuList.add(menu);
  }

  public List<IMenu> getPopupMenus() {
    return CollectionUtility.arrayList(m_popupMenuList);
  }
}
