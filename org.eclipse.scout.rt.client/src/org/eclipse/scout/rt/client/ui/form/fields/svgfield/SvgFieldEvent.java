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
package org.eclipse.scout.rt.client.ui.form.fields.svgfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.IScoutSVGElement;

public class SvgFieldEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  private final int m_type;
  private boolean m_consumed;
  private ArrayList<IMenu> m_popupMenus;
  private IScoutSVGElement m_selectedElement;

  /**
   * A new element was selected
   */
  public static final int TYPE_ELEMENT_SELECTED = 10;
  /**
   * The selected element was clicked
   */
  public static final int TYPE_ELEMENT_CLICKED = 20;
  /**
   * A popup is requested on the selected element
   */
  public static final int TYPE_ELEMENT_POPUP = 30;

  public SvgFieldEvent(ISvgField source, int type) {
    super(source);
    m_type = type;
  }

  public SvgFieldEvent(ISvgField source, int type, IScoutSVGElement elem) {
    super(source);
    m_type = type;
    m_selectedElement = elem;
  }

  public int getType() {
    return m_type;
  }

  public ISvgField getSvgField() {
    return (ISvgField) getSource();
  }

  public boolean isConsumed() {
    return m_consumed;
  }

  /**
   * Once a listener consumes this event it is not posted to successive listeners.
   */
  public void consume() {
    m_consumed = true;
  }

  public void addPopupMenu(IMenu menu) {
    if (menu != null) {
      if (m_popupMenus == null) m_popupMenus = new ArrayList<IMenu>();
      m_popupMenus.add(menu);
    }
  }

  /**
   * used by {@link #TYPE_ELEMENT_POPUP} to add actions
   */
  public void addPopupMenus(IMenu[] menus) {
    if (menus != null) {
      if (m_popupMenus == null) m_popupMenus = new ArrayList<IMenu>();
      m_popupMenus.addAll(Arrays.asList(menus));
    }
  }

  /**
   * used by {@link #TYPE_ELEMENT_POPUP} to add actions
   */
  public IMenu[] getPopupMenus() {
    if (m_popupMenus != null) return m_popupMenus.toArray(new IMenu[0]);
    else return new IMenu[0];
  }

  /**
   * used by {@link #TYPE_ELEMENT_POPUP} to add actions
   */
  public int getPopupMenuCount() {
    if (m_popupMenus != null) return m_popupMenus.size();
    else return 0;
  }

  public IScoutSVGElement getSelectedElement() {
    return m_selectedElement;
  }
}
