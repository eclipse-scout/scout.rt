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
package org.eclipse.scout.rt.client.ui.basic.calendar;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@SuppressWarnings({"serial", "squid:S2057"})
public class CalendarEvent extends java.util.EventObject implements IModelEvent {

  /**
   * valid properties: component
   */
  public static final int TYPE_COMPONENT_ACTION = 20;

  /**
   * Broadcast request to add actions for component popup valid properties: component add actions to: popupActions
   */
  public static final int TYPE_COMPONENT_POPUP = 30;

  /**
   * Broadcast request to add actions for "new" popup valid properties: add actions to: popupActions
   */
  public static final int TYPE_NEW_POPUP = 31;

  private final int m_type;
  private final CalendarComponent m_component;
  private List<IMenu> m_popupMenus; // lazy created

  public CalendarEvent(ICalendar source, int type) {
    super(source);
    m_type = type;
    m_component = null;
  }

  public CalendarEvent(ICalendar source, int type, CalendarComponent comp) {
    super(source);
    m_type = type;
    m_component = comp;
  }

  public ICalendar getCalendar() {
    return (ICalendar) getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }

  public CalendarComponent getComponent() {
    return m_component;
  }

  /**
   * used by {@value #TYPE_COMPONENT_POPUP} and {@link #TYPE_NEW_POPUP} to add actions
   */
  public void addPopupMenu(IMenu menu) {
    if (menu == null) {
      return;
    }
    if (m_popupMenus == null) {
      m_popupMenus = new ArrayList<IMenu>();
    }
    m_popupMenus.add(menu);
  }

  /**
   * used by {@value #TYPE_COMPONENT_POPUP} and {@link #TYPE_NEW_POPUP} to add actions
   */
  public void addPopupMenus(List<IMenu> menus) {
    if (menus == null) {
      return;
    }
    if (m_popupMenus == null) {
      m_popupMenus = new ArrayList<IMenu>(menus.size());
    }
    m_popupMenus.addAll(menus);
  }

  /**
   * used by {@value #TYPE_COMPONENT_POPUP} and {@link #TYPE_NEW_POPUP} to collect actions
   */
  public List<IMenu> getPopupMenus() {
    return CollectionUtility.arrayList(m_popupMenus);
  }

  /**
   * used by {@value #TYPE_COMPONENT_POPUP} and {@link #TYPE_NEW_POPUP} to collect actions
   */
  public int getPopupMenuCount() {
    if (m_popupMenus != null) {
      return m_popupMenus.size();
    }
    return 0;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("CalendarEvent[");
    // decode type
    try {
      Field[] f = getClass().getDeclaredFields();
      for (int i = 0; i < f.length; i++) {
        if (Modifier.isPublic(f[i].getModifiers())
            && Modifier.isStatic(f[i].getModifiers())
            && f[i].getName().startsWith("TYPE_")
            && ((Number) f[i].get(null)).intValue() == m_type) {
          buf.append(f[i].getName());
          break;
        }
      }
    }
    catch (Exception t) { // NOSONAR
      buf.append("#" + m_type);
    }
    if (m_component != null) {
      buf.append(", component=" + m_component.getItem().getSubject());
    }
    buf.append("]");
    return buf.toString();
  }
}
